////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.cutter.cuts;

import com.telenav.mesakit.map.cutter.Cut;
import com.telenav.mesakit.map.cutter.PbfRegionCutter;
import com.telenav.mesakit.map.cutter.cuts.maps.RegionNodes;
import com.telenav.mesakit.map.cutter.cuts.maps.RegionWays;
import com.telenav.mesakit.map.cutter.project.lexakai.diagrams.DiagramMapCutter;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.region.Region;
import com.telenav.kivakit.collections.map.MultiMap;
import com.telenav.kivakit.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.kernel.language.values.count.Bytes;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.FILTERED_OUT;

@SuppressWarnings({ "rawtypes", "ClassEscapesDefinedScope" })
@UmlClassDiagram(diagram = DiagramMapCutter.class)
public class SoftCut extends Cut
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** Nodes that are strictly inside each region */
    private final RegionNodes nodesInsideRegion;

    /** Ways that intersect with each region */
    private final RegionWays waysBelongingToRegion;

    /** The exterior regions for nodes that belong to a region due to a soft-cut way */
    private final MultiMap<Long, Integer> exteriorRegionsForNode = new MultiMap<>();

    public SoftCut(final PbfRegionCutter extractor, final RegionNodes regionNodes, final RegionWays regionWays)
    {
        super(extractor);
        nodesInsideRegion = regionNodes;
        waysBelongingToRegion = regionWays;
    }

    @Override
    public Set<Resource> cut()
    {
        // Analyze the data to determine which nodes and ways belong to which regions
        analyze();

        // Write out nodes and ways to region PBF files
        extractCells();

        // Close output files
        closeWriters();

        return outputResources();
    }

    private void analyze()
    {
        // Determine which nodes and ways belong to each region
        final var data = extractor().data().get();
        data.phase("Analyzing");
        data.process(new PbfDataProcessor()
        {
            @Override
            public void onEndNodes()
            {
                if (DEBUG.isDebugOn())
                {
                    final var size = JavaVirtualMachine.local().sizeOfObjectGraph(nodesInsideRegion(),
                            "nodesInsideRegion", Bytes.kilobytes(100));
                    LOGGER.information("nodesInsideRegion = $", size);
                }
            }

            @Override
            public void onEndWays()
            {
                if (DEBUG.isDebugOn())
                {
                    LOGGER.information("exteriorRegionsForNode = " + JavaVirtualMachine.local().sizeOfObjectGraph(
                            exteriorRegionsForNode(), "SoftCut.exteriorRegionsForNode", Bytes.kilobytes(100)));
                    LOGGER.information("waysBelongingToRegion = " + JavaVirtualMachine.local().sizeOfObjectGraph(
                            waysBelongingToRegion(), "SoftCut.waysBelongingToRegion", Bytes.kilobytes(100)));
                }
            }

            @Override
            public Action onNode(final PbfNode node)
            {
                // Add node to relevant regions
                for (final var region : regionsForLocation(location(node)))
                {
                    final var regionIndex = indexForRegion(region);
                    if (regionIndex != null)
                    {
                        checkRegionIndex(regionIndex);
                        nodesInsideRegion().add(node.identifierAsLong(), regionIndex);
                    }
                }
                return ACCEPTED;
            }

            @Override
            public Action onWay(final PbfWay way)
            {
                // If the way is included
                if (include(way))
                {
                    // get the regions that have way nodes
                    final var regionIndexes = nodesInsideRegion().regionIndexes(way);
                    checkRegionIndexes(regionIndexes);

                    // go through each region
                    for (final int regionIndex : regionIndexes)
                    {
                        // and for each way node
                        for (final var node : way.nodes())
                        {
                            // if the node is not in the region
                            final var nodeId = node.getNodeId();
                            if (!nodesInsideRegion().inRegion(nodeId, regionIndex))
                            {
                                // add it to the exterior regions
                                exteriorRegionsForNode().add(nodeId, regionIndex);
                            }
                        }

                        // the way belongs to the given region
                        waysBelongingToRegion().add(regionIndex, way.identifierAsLong());
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });
        LOGGER.information(AsciiArt.bottomLine(20, "Done Analyzing $", data.resource()));
    }

    private void checkRegionIndex(final Integer regionIndex)
    {
        assert nodesInsideRegion().regionIndexMap().isValidRegionIndex(regionIndex) : "Invalid region index "
                + regionIndex;
    }

    private void checkRegionIndexes(final Collection<Integer> regionIndexes)
    {
        assert nodesInsideRegion().regionIndexMap()
                .isValidRegionIndexList(regionIndexes) : "Invalid region index list: " + regionIndexes;
    }

    private MultiMap<Long, Integer> exteriorRegionsForNode()
    {
        return exteriorRegionsForNode;
    }

    private void extractCells()
    {
        // Determine which nodes belong to each region
        final var data = extractor().data().get();
        data.phase("Extracting");
        data.process(new PbfDataProcessor()
        {
            @Override
            public Action onNode(final PbfNode node)
            {
                // and any regions that strictly contain the location
                for (final var region : regionsForLocation(location(node)))
                {
                    // Get the region index
                    final var regionIndex = indexForRegion(region);
                    if (regionIndex != null)
                    {
                        // and if the node strictly belongs to the region,
                        if (nodesInsideRegion().inRegion(node.identifierAsLong(), regionIndex))
                        {
                            // write the node to the writer for the given region
                            final var writer = writer(region);
                            if (writer != null)
                            {
                                writer.write(node);
                            }
                        }
                    }
                }

                // Get any exterior regions this node might belong to
                final List<Integer> regionIndexes = exteriorRegionsForNode().get(node.identifierAsLong());
                if (regionIndexes != null)
                {
                    // Ensure region indexes
                    checkRegionIndexes(regionIndexes);

                    // Go through each exterior region
                    for (final var regionIndex : regionIndexes)
                    {
                        // and write the node to that region
                        final var writer = writer(regionForIndex(regionIndex));
                        if (writer != null)
                        {
                            writer.write(node);
                        }
                    }
                }
                return ACCEPTED;
            }

            @Override
            public Action onRelation(final PbfRelation relation)
            {
                // Go through members of the relation
                final var written = new HashSet<String>();
                for (final var member : relation.members())
                {
                    // and if the member is a way
                    if (member.getMemberType() == EntityType.Way)
                    {
                        // go through each region that the way belongs to
                        for (final var region : waysBelongingToRegion().regions(member.getMemberId()))
                        {
                            final var key = region.identifier().asInteger() + "-" + relation.identifierAsLong();
                            if (!written.contains(key))
                            {
                                written.add(key);

                                // and write out the relation (which may reference missing ways from
                                // neighboring regions) to that region
                                final var writer = writer(region);
                                if (writer != null)
                                {
                                    writer.write(relation);
                                }
                            }
                        }
                    }
                }
                return ACCEPTED;
            }

            @Override
            public Action onWay(final PbfWay way)
            {
                // and each region that the way belongs to
                for (final var region : waysBelongingToRegion().regions(way.identifierAsLong()))
                {
                    // Get the writer for the region
                    final var writer = writer(region);
                    if (writer != null)
                    {
                        // and write the way, with telenav:softcut=true if the way is soft-cut
                        writer.write(isSoftCut(region, way) ? withTelenavSoftCutTag(way) : way);
                    }
                }
                return ACCEPTED;
            }
        });
        LOGGER.information(AsciiArt.bottomLine(20, "Done Extracting Cells From $", data.resource()));
    }

    private boolean isSoftCut(final Region region, final PbfWay way)
    {
        // Get region index
        final var regionIndex = indexForRegion(region);
        if (regionIndex != null)
        {
            // Ensure region index
            checkRegionIndex(regionIndex);

            // Go through nodes
            for (final var node : way.nodes())
            {
                // and if there's a way node not inside this region
                if (!nodesInsideRegion().inRegion(node.getNodeId(), regionIndex))
                {
                    // then the way is soft-cut by definition
                    return true;
                }
            }
        }
        return false;
    }

    private RegionNodes nodesInsideRegion()
    {
        return nodesInsideRegion;
    }

    private RegionWays waysBelongingToRegion()
    {
        return waysBelongingToRegion;
    }
}
