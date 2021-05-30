open module tdk.graph.world
{
    requires transitive tdk.map.utilities.geojson;
    requires transitive tdk.map.cutter;
    requires transitive tdk.graph.core;

    exports com.telenav.tdk.graph.world;
    exports com.telenav.tdk.graph.world.grid;
    exports com.telenav.tdk.graph.world.identifiers;
    exports com.telenav.tdk.graph.world.project;
    exports com.telenav.tdk.graph.world.repository;
}
