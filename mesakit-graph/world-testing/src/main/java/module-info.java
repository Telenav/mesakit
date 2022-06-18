open module mesakit.graph.world.testing
{
    requires transitive mesakit.map.utilities.geojson;
    requires transitive mesakit.map.cutter;
    requires transitive mesakit.graph.core;
    requires transitive mesakit.internal.graph.core.testing;
    requires transitive mesakit.graph.world;

    requires transitive kivakit.settings;

    exports com.telenav.mesakit.graph.world.testing;
}
