open module mesakit.graph.world
{
    requires transitive tdk.map.utilities.geojson;
    requires transitive tdk.map.cutter;
    requires transitive mesakit.graph.core;

    exports com.telenav.kivakit.graph.world;
    exports com.telenav.kivakit.graph.world.grid;
    exports com.telenav.kivakit.graph.world.identifiers;
    exports com.telenav.kivakit.graph.world.project;
    exports com.telenav.kivakit.graph.world.repository;
    exports com.telenav.mesakit.graph.world.grid;
    exports com.telenav.mesakit.graph.world.repository;
    exports com.telenav.mesakit.graph.world;
}
