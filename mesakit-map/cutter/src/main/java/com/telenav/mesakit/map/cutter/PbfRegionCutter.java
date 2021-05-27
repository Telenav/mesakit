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

package com.telenav.mesakit.map.cutter;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.interfaces.value.Source;
import com.telenav.kivakit.kernel.language.primitives.Booleans;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.ResourceList;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.UmlMethodGroup;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeMember;
import com.telenav.mesakit.map.cutter.cuts.FastCut;
import com.telenav.mesakit.map.cutter.cuts.SoftCut;
import com.telenav.mesakit.map.cutter.cuts.maps.RegionIndexMap;
import com.telenav.mesakit.map.cutter.cuts.maps.RegionNodes;
import com.telenav.mesakit.map.cutter.cuts.maps.RegionWays;
import com.telenav.mesakit.map.cutter.project.lexakai.diagrams.DiagramMapCutter;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionSet;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({ "rawtypes", "ClassEscapesDefinedScope" })
@UmlClassDiagram(diagram = DiagramMapCutter.class)
@UmlRelation(label = "cuts with", referent = Cut.class)
public class PbfRegionCutter
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Source<PbfDataSource> data;

    private final Folder outputFolder;

    @UmlAggregation(label = "filters ways with")
    private final WayFilter wayFilter;

    @UmlAggregation(label = "regions to extract")
    private RegionSet regionsToExtract;

    private RegionIndexMap regionIndexMap;

    public PbfRegionCutter(final Source<PbfDataSource> data, final Folder outputFolder, final WayFilter wayFilter)
    {
        this.data = data;
        this.outputFolder = outputFolder;
        this.wayFilter = wayFilter;
        if (!outputFolder.exists())
        {
            outputFolder.mkdirs();
        }
    }

    @UmlMethodGroup("cutting")
    public ResourceList cut()
    {
        LOGGER.information("Extracting $ cell(s) from $", regionsToExtract.size(), data.get().resource());

        // Cut the desired regions from the input file
        final Cut cut;
        if (hasWayNodeLocations(data.get()))
        {
            cut = new FastCut(this, regionWays());
        }
        else
        {
            cut = new SoftCut(this, regionNodes(), regionWays());
        }
        cut.cut();

        // Remove any zero-size output files and their parent folders
        final Set<Resource> outputs = new HashSet<>();
        for (final var resource : cut.outputResources())
        {
            if (resource.exists())
            {
                if (resource.bytes().isZero())
                {
                    if (resource instanceof File)
                    {
                        final var file = (File) resource;
                        file.delete();
                        if (file.parent().isEmpty())
                        {
                            file.parent().delete();
                        }
                    }
                }
                else
                {
                    outputs.add(resource);
                }
            }
        }
        final var outputResources = new ResourceList(outputs);
        LOGGER.information(AsciiArt.textBox("Output Files", "$\n$", outputFolder,
                AsciiArt.bulleted(outputResources.relativeTo(outputFolder))));
        return outputResources;
    }

    @UmlExcludeMember
    public Source<PbfDataSource> data()
    {
        return data;
    }

    @KivaKitIncludeProperty
    @UmlExcludeMember
    public Folder outputFolder()
    {
        return outputFolder;
    }

    @UmlExcludeMember
    public RegionIndexMap regionIndexMap()
    {
        if (regionIndexMap == null)
        {
            regionIndexMap = newRegionIndexMap();
        }
        return regionIndexMap;
    }

    @UmlExcludeMember
    public List<Region> regionsForLocation(final Location location)
    {
        // List of regions we're extracting that include the location
        final List<Region> regions = new ArrayList<>();

        // Find any metropolitan area for the location and if we're extracting it, add it to the
        // list
        final var metropolitanArea = MetropolitanArea.forLocation(location);
        if (metropolitanArea != null && regionsToExtract.contains(metropolitanArea))
        {
            regions.add(metropolitanArea);
        }

        // Find any county for the location and if we're extracting it, add it to the list
        final var county = County.forLocation(location);
        if (county != null && regionsToExtract.contains(county))
        {
            regions.add(county);
        }

        // Find any state for the location and if we're extracting it, add it to the list
        final State state;
        if (metropolitanArea != null)
        {
            state = metropolitanArea.state();
        }
        else if (county != null)
        {
            state = county.state();
        }
        else
        {
            state = State.forLocation(location);
        }
        if (state != null && regionsToExtract.contains(state))
        {
            regions.add(state);
        }

        // Find any country for the location and if we're extracting it, add it to the list
        final var country = state != null ? state.country() : Country.forLocation(location);
        if (country != null && regionsToExtract.contains(country))
        {
            regions.add(country);
        }

        return regions;
    }

    @KivaKitIncludeProperty
    @UmlExcludeMember
    public RegionSet regionsToExtract()
    {
        return regionsToExtract;
    }

    public void regionsToExtract(final RegionSet regionsToExtract)
    {
        this.regionsToExtract = regionsToExtract;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    @UmlExcludeMember
    public WayFilter wayFilter()
    {
        return wayFilter;
    }

    @UmlExcludeMember
    protected RegionIndexMap newRegionIndexMap()
    {
        final var regionIndexMap = new RegionIndexMap();
        for (final var region : regionsToExtract())
        {
            regionIndexMap.add(region);
        }
        return regionIndexMap;
    }

    /**
     * @return True if the data has {@link WayNode} locations available. This can speed up processing and reduce memory
     * consumption by not requiring that {@link Node} locations be kept in memory until {@link Way} processing occurs.
     */
    private boolean hasWayNodeLocations(final PbfDataSource data)
    {
        final var metadata = data.metadata();
        return metadata != null && Booleans.isTrue(metadata.get(WayNode.METADATA_KEY_LOCATION_INCLUDED));
    }

    private RegionNodes regionNodes()
    {
        return new RegionNodes("PbfRegionCutter.regionNodes", regionIndexMap());
    }

    private RegionWays regionWays()
    {
        return new RegionWays("PbfRegionCutter.regionWays", regionIndexMap());
    }
}
