////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.graph.specifications.library.pbf;

import com.telenav.kivakit.collections.primitive.set.SplitLongSet;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.kernel.scalars.bytes.Bytes;
import com.telenav.kivakit.kernel.scalars.mutable.MutableValue;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.compression.*;
import com.telenav.kivakit.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.kivakit.data.formats.pbf.processing.filters.*;
import com.telenav.kivakit.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.kivakit.data.formats.pbf.processing.writers.PbfWriter;
import com.telenav.kivakit.graph.Metadata;
import com.telenav.kivakit.graph.metadata.DataBuild;
import com.telenav.kivakit.map.geography.rectangle.BoundingBoxBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.*;

import static com.telenav.kivakit.data.formats.pbf.processing.PbfDataProcessor.Result.*;

/**
 * Reads and writes PBF file metadata. The {@link #read()} method reads the given file to determine as much metadata as
 * possible. The {@link #write(Metadata)} method adds metadata to the file if it does not contain metadata already or
 * replaces existing metadata if it does.
 *
 * @author jonathanl (shibo)
 * @see Metadata
 * @see File
 */
public class PbfFileMetadataAnnotator extends BaseRepeater<Message>
{
    public enum Mode
    {
        RETAIN_ALL,
        STRIP_UNREFERENCED_NODES
    }

    private final File file;

    private final Mode mode;

    private final WayFilter wayFilter;

    private final RelationFilter relationFilter;

    private final SplitLongSet retain = (SplitLongSet) new SplitLongSet("keep")
            .initialSize(1_000_000)
            .initialize();

    private PbfTagCodecBuilder codecBuilder;

    private PbfCharacterCodecBuilder characterCodecBuilder;

    /**
     * @param file The file to read from or write to
     */
    public PbfFileMetadataAnnotator(final File file, final Mode mode, final WayFilter wayFilter,
                                    final RelationFilter relationFilter)
    {
        this.file = file;
        this.mode = mode;
        this.wayFilter = wayFilter;
        this.relationFilter = relationFilter;
    }

    /**
     * Reads the entire file to determine the number of ways, nodes and relations as well as the bounding rectangle of
     * the data. Other metadata such as the build time and size are included, but since the descriptor and precision
     * cannot be determined by reading the file they are not returned.
     *
     * @return The partial metadata (without the descriptor or data precision) determined by reading the file
     */
    public Metadata read()
    {
        final var bounds = new BoundingBoxBuilder();
        codecBuilder = new PbfTagCodecBuilder();
        characterCodecBuilder = new PbfCharacterCodecBuilder();
        final var statistics = reader().process("Processing", new PbfDataProcessor()
        {
            @Override
            public Result onNode(final PbfNode node)
            {
                bounds.add(node.latitude(), node.longitude());

                if (mode == Mode.STRIP_UNREFERENCED_NODES)
                {
                    // If the node has a place tag
                    if (node.hasKey("place"))
                    {
                        // then we want to keep this node
                        retain.add(node.identifierAsLong());
                    }
                }
                return ACCEPTED;
            }

            @Override
            public Result onRelation(final PbfRelation relation)
            {
                if (relationFilter.accepts(relation))
                {
                    characterCodecBuilder.sample(relation);
                    codecBuilder.sample(relation);
                    if (mode == Mode.STRIP_UNREFERENCED_NODES)
                    {
                        // Keep any nodes that are members of a relation
                        for (final var member : relation.members())
                        {
                            if (member.getMemberType() == EntityType.Node)
                            {
                                retain.add(member.getMemberId());
                            }
                        }
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }

            @Override
            public Result onWay(final PbfWay way)
            {
                if (wayFilter.accepts(way))
                {
                    characterCodecBuilder.sample(way);
                    codecBuilder.sample(way);
                    if (mode == Mode.STRIP_UNREFERENCED_NODES)
                    {
                        // Keep any nodes that are referenced by a way
                        for (final var node : way.nodes())
                        {
                            retain.add(node.getNodeId());
                        }
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });

        return new Metadata()
                .withDataBuild(DataBuild.at(file.lastModified().localTime()))
                .withDataSize(file.size())
                .withDataBounds(bounds.build())
                .withWayCount(statistics.ways())
                .withNodeCount(mode == Mode.RETAIN_ALL ? statistics.nodes() : retain.count())
                .withRelationCount(statistics.relations());
    }

    /**
     * Adds or replaces any metadata in the file with the given metadata
     *
     * @param metadata The metadata to add to the file or which should replace any existing metadata
     * @return True if the given metadata was successfully added or if it successfully replaced existing metadata
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public boolean write(final Metadata metadata)
    {
        final var reader = reader();
        final var temporary = file.withFileName(file.fileName().withExtension(Extension.TMP));
        final var writer = new PbfWriter(temporary, false);

        final var processedFirstNode = new MutableValue<>(false);
        final var wroteCompleteFile = new MutableValue<>(false);

        final var metadataNodeIdentifier = 99_999_999_999L;

        final var statistics = reader.process("Updating Metadata", new PbfDataProcessor()
        {
            @Override
            public void onBounds(final Bound bounds)
            {
                writer.write(bounds);
            }

            @Override
            public void onEndRelations()
            {
                wroteCompleteFile.set(true);
            }

            @Override
            public void onEndWays()
            {
                if (metadata.relationCount(Metadata.CountType.ALLOW_ESTIMATE).isZero())
                {
                    wroteCompleteFile.set(true);
                }
            }

            @Override
            public Result onNode(final PbfNode node)
            {
                if (!processedFirstNode.get())
                {
                    try
                    {
                        // Write out metadata node
                        final var tags = new ArrayList<Tag>();

                        tags.add(new Tag("telenav-data-descriptor", metadata.descriptor()));
                        tags.add(new Tag("telenav-data-size", metadata.dataSize().toString()));
                        tags.add(new Tag("telenav-data-build", metadata.dataBuild().toString()));
                        tags.add(new Tag("telenav-data-precision", metadata.dataPrecision().toString()));
                        tags.add(new Tag("telenav-data-bounds", metadata.dataBounds().toString()));
                        tags.add(new Tag("telenav-data-nodes", metadata.nodeCount(Metadata.CountType.REQUIRE_EXACT).toString()));
                        tags.add(new Tag("telenav-data-ways", metadata.wayCount(Metadata.CountType.REQUIRE_EXACT).toString()));
                        tags.add(new Tag("telenav-data-relations", metadata.relationCount(Metadata.CountType.REQUIRE_EXACT).toString()));

                        codecBuilder.buildCharacterCodecs();
                        codecBuilder.buildStringCodecs();

                        tags.addAll(PbfTags.tags("telenav-key-character-codec-", codecBuilder.keyCharacterCodec().asProperties()));
                        tags.addAll(PbfTags.tags("telenav-key-string-codec-", codecBuilder.keyStringCodec().asProperties()));

                        tags.addAll(PbfTags.tags("telenav-value-character-codec-", codecBuilder.valueCharacterCodec().asProperties()));
                        tags.addAll(PbfTags.tags("telenav-value-string-codec-", codecBuilder.valueStringCodec().asProperties()));

                        tags.addAll(PbfTags.tags("telenav-road-name-character-codec-", characterCodecBuilder.build().asProperties()));

                        final var entityData = new CommonEntityData(metadataNodeIdentifier, 1, new Date(), new OsmUser(99_999_999, "telenav"), 1L, tags);
                        final var metadataNode = new PbfNode(new Node(entityData, 0.0, 0.0));
                        writer.write(metadataNode);

                        // If the node we read was a metadata nodes
                        if (node.identifierAsLong() == metadataNodeIdentifier)
                        {
                            // then we just throw it out since we added a fresh one
                            return FILTERED_OUT;
                        }
                    }
                    finally
                    {
                        processedFirstNode.set(true);
                    }
                }

                // If we're retaining all nodes or the node is in the list of nodes to retain
                if (mode == Mode.RETAIN_ALL || retain.contains(node.identifierAsLong()))
                {
                    // the write it out
                    writer.write(node);
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }

            @Override
            public Result onRelation(final PbfRelation relation)
            {
                writer.write(relation);
                return ACCEPTED;
            }

            @Override
            public Result onWay(final PbfWay way)
            {
                if (wayFilter.accepts(way))
                {
                    writer.write(way);
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });

        writer.close();

        if (statistics != null && wroteCompleteFile.get())
        {
            file.delete();
            temporary.renameTo(file);
            return true;
        }
        else
        {
            temporary.delete();
            return false;
        }
    }

    private SerialPbfReader reader()
    {
        final var reader = new SerialPbfReader(file);
        if (file.size().isGreaterThan(Bytes.megabytes(20)))
        {
            listenTo(reader);
        }
        else
        {
            reader.silence();
        }
        return reader;
    }
}
