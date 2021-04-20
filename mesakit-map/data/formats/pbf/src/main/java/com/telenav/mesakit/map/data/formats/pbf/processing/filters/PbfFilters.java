package com.telenav.mesakit.map.data.formats.pbf.processing.filters;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmExcludeNonDrivableWaysFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmMajorRoadsWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmNavigableWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmRelationsFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmWaysFilter;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessingFilters;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessingFilters.class)
public class PbfFilters
{
    public static void loadAll()
    {
        // OSM filters
        new OsmExcludeNonDrivableWaysFilter();
        new OsmMajorRoadsWayFilter();
        new OsmNavigableWayFilter();
        new OsmWaysFilter();
        new OsmRelationsFilter();
    }
}
