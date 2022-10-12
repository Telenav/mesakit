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
import com.telenav.kivakit.core.string.Formatter;
import com.telenav.kivakit.core.string.ObjectIndenter;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import com.telenav.kivakit.primitive.collections.set.SplitLongSet;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.PbfAllNodeDiskStores;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.Collection;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;

/**
 * Holds information obtained by analyzing an PBF resource, including basic information:
 * <ul>
 *     <li>The bounding rectangle of the data</li>
 *     <li>The number of nodes, ways and relations</li>
 *     <li>The highest entity identifier found in the data</li>
 * </ul>
 * Relation and node information:
 * <ul>
 *     <li>The node identifiers of intersections</li>
 *     <li>Relation information, including maps from relation identifier and way identifier to relation</li>
 *     <li>Optionally, complete node information stored on disk</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
public class PbfDataAnalysis extends BaseRepeater
{
    public enum AnalysisType
    {
        /** Build tag codecs, find intersections and compute statistics */
        DEFAULT,

        /**
         * Build tag codecs, find intersections, compute statistics and store complete node information on disk
         */
        FULL_NODE_INFORMATION
    }

    /** The type of analysis to perform */
    private final AnalysisType analysisType;

    /** Bounds of PBF resource */
    private Rectangle bounds;

    /** True if this PBF file has way node locations */
    private boolean hasWayNodeLocations;

    /** The highest PBF identifier read so far */
    private long highestNodeIdentifier = Long.MIN_VALUE;

    /** Node count map for finding intersections */
    private IntersectionMap intersections;

    /**
     * Metadata for allocation sizing
     */
    private Metadata metadata;

    /** Disk stores with full node information */
    private PbfAllNodeDiskStores pbfNodeDiskStores;

    /** Way filter to restrict which ways are included */
    private final WayFilter wayFilter;

    /**
     * Way nodes for the second pass so we can store information about only the nodes we will actually use
     */
    private SplitLongSet wayNodes;

    /**
     * Constructs a set of codecs for encoding nodes, ways and relations from an PBF data source
     */
    public PbfDataAnalysis(Metadata metadata, AnalysisType analysisType, WayFilter wayFilter)
    {
        ensure(metadata != null);
        ensure(analysisType != null);
        ensure(wayFilter != null);

        this.metadata = metadata;
        this.analysisType = analysisType;
        this.wayFilter = wayFilter;

        wayNodes = new SplitLongSet("data-analysis.way-nodes");
        wayNodes.initialSize(metadata.wayCount(ALLOW_ESTIMATE).asEstimate());
        wayNodes.initialize();

        intersections = new IntersectionMap("data-analysis.intersections");
    }

    public void analyze(PbfDataSource input)
    {
        var fileName = input.resource().fileName();
        metadata.configure(input);

        // Build bounding box around data
        var builder = new BoundingBoxBuilder();

        // Initialize disk stores if we're storing full PBF information
        if (analysisType == AnalysisType.FULL_NODE_INFORMATION)
        {
            pbfNodeDiskStores = new PbfAllNodeDiskStores(PbfAllNodeDiskStores.temporary(), fileName);
        }

        // Process input data
        var statistics = input.process(new PbfDataProcessor()
        {
            @Override
            public void onEndWays()
            {
                intersections().doneAdding();
            }

            @Override
            public Action onNode(PbfNode node)
            {
                processNode(node, builder);
                return ACCEPTED;
            }

            @Override
            public void onNodes(Collection<PbfNode> nodes)
            {
                for (var node : nodes)
                {
                    processNode(node, builder);
                }
            }

            @Override
            public Action onWay(PbfWay way)
            {
                processWay(way);
                return ACCEPTED;
            }

            @Override
            public void onWays(Collection<PbfWay> ways)
            {
                for (var way : ways)
                {
                    processWay(way);
                }
            }
        });

        bounds = builder.build();

        metadata = metadata
                .withDataBounds(bounds)
                .withNodeCount(statistics.nodes())
                .withWayCount(statistics.ways())
                .withRelationCount(statistics.relations());

        var indenter = new ObjectIndenter(StringFormattable.Format.USER_MULTILINE);
        indenter.indented("metadata", () -> metadata().asString(StringFormattable.Format.USER_MULTILINE, indenter));
        information(AsciiArt.textBox(Formatter.format("PBF Data Analysis of $", fileName), indenter.toString()));
    }

    public void freeIntersectionMap()
    {
        intersections = null;
    }

    public void freeWayNodes()
    {
        wayNodes = null;
    }

    /**
     * Returns true if the PBF data includes way node locations. This is determined by looking at {@link
     * WayNode#getLatitude()} and {@link WayNode#getLongitude()} rather than at the PBF metadata, since looking at the
     * actual values is more likely to be accurate than looking at the metadata in the header.
     */
    public boolean hasWayNodeLocations()
    {
        return hasWayNodeLocations;
    }

    public MapNodeIdentifier highestNodeIdentifier()
    {
        return new PbfNodeIdentifier(highestNodeIdentifier);
    }

    public IntersectionMap intersections()
    {
        return intersections;
    }

    public boolean isValid()
    {
        return intersections != null && bounds != null;
    }

    /** The metadata after analysis */
    public Metadata metadata()
    {
        return metadata;
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    public PbfAllNodeDiskStores pbfNodeDiskStores()
    {
        return pbfNodeDiskStores;
    }

    public SplitLongSet wayNodes()
    {
        return wayNodes;
    }

    private void expandBounds(BoundingBoxBuilder boundsBuilder, PbfNode node)
    {
        var latitude = node.latitude();
        var longitude = node.longitude();
        if (latitude > 90 || latitude < -90)
        {
            warning("PBF node $ has invalid latitude of $", node.identifierAsLong(), latitude);
        }
        if (longitude > 180 || longitude < -180)
        {
            warning("PBF node $ has invalid longitude of $", node.identifierAsLong(), longitude);
        }
        boundsBuilder.add(latitude, longitude);
    }

    private void processNode(PbfNode node, BoundingBoxBuilder builder)
    {
        // Possibly increase the data bounds
        expandBounds(builder, node);

        // Increase references to barriers and signals
        referenceBarriersAndSignals(node);

        // If we're storing nodes on disk (full node information)
        if (analysisType == AnalysisType.FULL_NODE_INFORMATION)
        {
            // then add the node
            pbfNodeDiskStores.add(node);
        }
    }

    private void processWay(PbfWay way)
    {
        // If we accept this way
        if (wayFilter.accepts(way))
        {
            // Possibly increase the highest identifier
            highestNodeIdentifier = Math.max(highestNodeIdentifier, way.identifierAsLong());

            // Increase reference counts to way nodes and store identifiers for later
            processWayNodes(way);
        }
    }

    private synchronized void processWayNodes(PbfWay way)
    {
        // Get way nodes
        var wayNodes = way.nodes();

        // Go through nodes
        var i = 0;
        for (var node : wayNodes)
        {
            // Get node id and add it to the set of way nodes we know about
            var nodeIdentifier = node.getNodeId();
            this.wayNodes.add(nodeIdentifier);

            // Possibly increase the highest node identifier
            highestNodeIdentifier = Math.max(highestNodeIdentifier, nodeIdentifier);

            // If way nodes have locations, make a note of it
            hasWayNodeLocations = hasWayNodeLocations
                    || (node.getLatitude() != 0 && node.getLongitude() != 0);

            // The end-point segment counts are incremented once, while the interior
            // point segment counts are incremented twice (because they are used by
            // two segments). Any location that has a segment count of more than 2 is
            // an intersection.
            reference(nodeIdentifier);
            if (i > 0 && i < wayNodes.size() - 1)
            {
                reference(nodeIdentifier);
            }
            i++;
        }
    }

    private void reference(long nodeIdentifier)
    {
        intersections.addReference(nodeIdentifier);
    }

    private void referenceBarriersAndSignals(PbfNode node)
    {
        // HOTSPOT: This method has been determined to be a hotspot by YourKit profiling

        String signal = null;
        String barrier = null;
        for (var tag : node)
        {
            if ("barrier".equals(tag.getKey()))
            {
                barrier = tag.getValue();
                break;
            }
            if ("signal".equals(tag.getKey()))
            {
                signal = tag.getValue();
                break;
            }
        }
        if (barrier != null || signal != null)
        {
            // A barrier is marked as an intersection so it will be broken up by
            // the edge sectioner when it sections intersections
            var identifier = node.identifierAsLong();
            reference(identifier);
            reference(identifier);
            reference(identifier);
        }
    }
}
