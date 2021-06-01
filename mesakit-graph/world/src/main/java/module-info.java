open module mesakit.graph.world
{
    requires transitive mesakit.map.utilities.geojson;
    requires transitive mesakit.map.cutter;
    requires transitive mesakit.graph.core;

    exports com.telenav.mesakit.graph.world;
    exports com.telenav.mesakit.graph.world.grid;
    exports com.telenav.mesakit.graph.world.identifiers;
    exports com.telenav.mesakit.graph.world.project;
    exports com.telenav.mesakit.graph.world.repository;
}
