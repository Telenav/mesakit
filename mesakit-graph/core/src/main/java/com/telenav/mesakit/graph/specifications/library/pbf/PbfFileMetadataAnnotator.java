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

package com.telenav.mesakit.graph.specifications.library.pbf;

import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.core.value.mutable.MutableValue;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.primitive.collections.set.SplitLongSet;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.metadata.DataBuild;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTags;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfCharacterCodecBuilder;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfTagCodecBuilder;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.data.formats.pbf.processing.writers.PbfWriter;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Date;

import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.FILTERED_OUT;

/**
 * Reads and writes PBF file metadata. The {@link #read()} method reads the given file to determine as much metadata as
 * possible. The {@link #write(Metadata)} method adds metadata to the file if it does not contain metadata already or
 * replaces existing metadata if it does.
 *
 * @author jonathanl (shibo)
 * @see Metadata
 * @see File
 */
public class PbfFileMetadataAnnotator extends BaseRepeater
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

    private final SplitLongSet retain;

    private PbfTagCodecBuilder codecBuilder;

    private PbfCharacterCodecBuilder characterCodecBuilder;

    /**
     * @param file The file to read from or write to
     */
    public PbfFileMetadataAnnotator(File file, Mode mode, WayFilter wayFilter,
                                    RelationFilter relationFilter)
    {
        this.file = file;
        this.mode = mode;
        this.wayFilter = wayFilter;
        this.relationFilter = relationFilter;

        retain = (SplitLongSet) new SplitLongSet("keep").initialSize(1_000_000);
        retain.initialize();
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
        var bounds = new BoundingBoxBuilder();
        codecBuilder = new PbfTagCodecBuilder();
        characterCodecBuilder = new PbfCharacterCodecBuilder();
        var statistics = reader().process(new PbfDataProcessor()
        {
            @Override
            public Action onNode(PbfNode node)
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
            public Action onRelation(PbfRelation relation)
            {
                if (relationFilter.accepts(relation))
                {
                    characterCodecBuilder.sample(relation);
                    codecBuilder.sample(relation);
                    if (mode == Mode.STRIP_UNREFERENCED_NODES)
                    {
                        // Keep any nodes that are members of a relation
                        for (var member : relation.members())
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
            public Action onWay(PbfWay way)
            {
                if (wayFilter.accepts(way))
                {
                    characterCodecBuilder.sample(way);
                    codecBuilder.sample(way);
                    if (mode == Mode.STRIP_UNREFERENCED_NODES)
                    {
                        // Keep any nodes that are referenced by a way
                        for (var node : way.nodes())
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
                .withDataSize(file.sizeInBytes())
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
    public boolean write(Metadata metadata)
    {
        var reader = reader();
        var temporary = file.parent().file(file.fileName().withExtension(Extension.TMP));
        var writer = new PbfWriter(temporary, false);

        var processedFirstNode = new MutableValue<>(false);
        var wroteCompleteFile = new MutableValue<>(false);

        var metadataNodeIdentifier = 99_999_999_999L;

        var statistics = reader.process(new PbfDataProcessor()
        {
            @Override
            public void onBounds(Bound bounds)
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
            public Action onNode(PbfNode node)
            {
                if (!processedFirstNode.get())
                {
                    try
                    {
                        // Write out metadata node
                        var tags = new ArrayList<Tag>();

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

                        var entityData = new CommonEntityData(metadataNodeIdentifier, 1, new Date(), new OsmUser(99_999_999, "telenav"), 1L, tags);
                        var metadataNode = new PbfNode(new Node(entityData, 0.0, 0.0));
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
                    // then write it out
                    writer.write(node);
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }

            @Override
            public Action onRelation(PbfRelation relation)
            {
                writer.write(relation);
                return ACCEPTED;
            }

            @Override
            public Action onWay(PbfWay way)
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
        var reader = new SerialPbfReader(file);
        if (file.sizeInBytes().isGreaterThan(Bytes.megabytes(20)))
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
