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

import com.telenav.kivakit.core.collections.map.MultiMap;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.cutter.Cut;
import com.telenav.mesakit.map.cutter.PbfRegionCutter;
import com.telenav.mesakit.map.cutter.cuts.maps.RegionWays;
import com.telenav.mesakit.map.cutter.project.lexakai.DiagramMapCutter;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.region.Region;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.util.List;
import java.util.Set;

import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.FILTERED_OUT;

/**
 * The FastCut PBF extractor is designed to work with PBF files that have been pre-processed by the OSM osmium command
 * line tool with the "add-locations-to-ways" feature. These files will have location information on way-nodes and
 * should further not include any nodes that don't have tags (unless they have been explicitly included with
 * "--keep-untagged-nodes", which is NOT generally recommended for efficiency reasons), allowing much lower memory usage
 * during extraction.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("rawtypes")
@UmlClassDiagram(diagram = DiagramMapCutter.class)
public class FastCut extends Cut
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /** Regions for nodes that belong to ways that span regions */
    private final MultiMap<MapNodeIdentifier, Region> regionsForNode = new MultiMap<>();

    /** Ways that intersect with each region */
    private final RegionWays regionWays;

    @SuppressWarnings("ClassEscapesDefinedScope")
    public FastCut(PbfRegionCutter extractor, RegionWays regionWays)
    {
        super(extractor);
        this.regionWays = regionWays;
    }

    @Override
    public Set<Resource> cut()
    {
        try
        {
            // Analyze the data to determine which nodes and ways belong to which regions
            analyze();

            // Write out nodes and ways to region PBF files
            output();

            // Close output files
            closeWriters();
        }
        catch (Throwable e)
        {
            LOGGER.problem(e, "Cutting PBF failed");
        }

        return outputResources();
    }

    private void analyze()
    {
        // Determine which nodes belong to each region
        var data = extractor().data().get();
        data.phase("Analysis");
        data.process(new PbfDataProcessor()
        {
            @Override
            public Action onWay(PbfWay way)
            {
                // If the way is included
                if (include(way))
                {
                    // get the regions that the way is included in or which it spans
                    var regions = regions(way);

                    // and if the way spans more than one region (cells in the world graph)
                    if (regions.size() > 1)
                    {
                        // then go through each node in the way,
                        for (var node : way.nodes())
                        {
                            // and record that the node belongs to all the regions that the way
                            // spans so we can output the node to each region's output file during
                            // the output pass
                            regionsForNode().addAll(new PbfNodeIdentifier(node), regions);
                        }
                    }

                    // Record which regions the way belongs to for the output pass
                    for (var region : regions)
                    {
                        regionWays().add(region, way);
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });
    }

    private void output()
    {
        // Determine which nodes belong to each region
        var data = extractor().data().get();
        data.phase("Extracting");
        data.process(new PbfDataProcessor()
        {
            @Override
            public Action onNode(PbfNode node)
            {
                // Get any list of regions if the node is involved in a way that spans regions
                List<Region> regions = regionsForNode().get(new PbfNodeIdentifier(node));

                // If the node doesn't span regions,
                if (regions == null)
                {
                    // get the regions it directly belongs to
                    regions = regionsForLocation(location(node));
                }

                // Write the node to the output file for each of the given regions
                write(node, regions);
                return ACCEPTED;
            }

            @Override
            public Action onRelation(PbfRelation relation)
            {
                // Go through members of the relation
                for (var member : relation.members())
                {
                    // and if the member is a way
                    if (member.getMemberType() == EntityType.Way)
                    {
                        // write the relation to each region that the way spans
                        write(relation, regionWays().regions(member.getMemberId()));
                    }
                }
                return ACCEPTED;
            }

            @Override
            public Action onWay(PbfWay way)
            {
                // Get any list of regions for this way if it spans regions
                var regions = regionWays().regions(way.identifierAsLong());

                // The way is soft-cut if it spans regions
                var softcut = regions.size() > 1;

                // Write the way to the output files for each region
                write(way, regions, softcut);
                return ACCEPTED;
            }
        });
    }

    private RegionWays regionWays()
    {
        return regionWays;
    }

    private MultiMap<MapNodeIdentifier, Region> regionsForNode()
    {
        return regionsForNode;
    }
}
