package com.telenav.mesakit.map.data.formats.pbf.processing.filters;

import com.telenav.mesakit.map.data.formats.pbf.processing.filters.navteam.NavTeamWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmExcludeNonDrivableWaysFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmMajorRoadsWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmNavigableWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmRelationsFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmWaysFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osmteam.OsmTeamDataPipelineWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osmteam.OsmTeamWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.unidb.UniDbExcludeAdasRelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.unidb.UniDbNavigableWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.unidb.UniDbRelationsFilter;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessingFilters;
import com.telenav.lexakai.annotations.UmlClassDiagram;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessingFilters.class)
public class PbfFilters
{
    public static void loadAll()
    {
        // NavTeam filters
        new NavTeamWayFilter();

        // OSM filters
        new OsmExcludeNonDrivableWaysFilter();
        new OsmMajorRoadsWayFilter();
        new OsmNavigableWayFilter();
        new OsmWaysFilter();
        new OsmRelationsFilter();

        // OsmTeam filters
        new OsmTeamWayFilter();
        new OsmTeamDataPipelineWayFilter();

        // UniDb filters
        new UniDbNavigableWayFilter();
        new UniDbRelationsFilter();
        new UniDbExcludeAdasRelationFilter();
    }
}
