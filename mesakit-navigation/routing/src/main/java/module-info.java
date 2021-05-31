open module mesakit.navigation.routing
{
    requires transitive mesakit.graph.world;
    requires transitive mesakit.map.ui.desktop;
    requires transitive mesakit.map.region;

    exports com.telenav.kivakit.navigation.routing;
    exports com.telenav.kivakit.navigation.routing.promoters;
    exports com.telenav.kivakit.navigation.routing.bidijkstra;
    exports com.telenav.kivakit.navigation.routing.limiters;
    exports com.telenav.kivakit.navigation.routing.cost;
    exports com.telenav.kivakit.navigation.routing.cost.operators;
    exports com.telenav.kivakit.navigation.routing.cost.functions;
    exports com.telenav.kivakit.navigation.routing.cost.functions.heuristic;
    exports com.telenav.kivakit.navigation.routing.debuggers;
    exports com.telenav.kivakit.navigation.routing.dijkstra;
}
