open module mesakit.navigation.routing
{
    requires transitive mesakit.graph.world;
    requires transitive mesakit.map.ui.desktop;
    requires transitive mesakit.map.region;

    exports com.telenav.mesakit.navigation.routing;
    exports com.telenav.mesakit.navigation.routing.promoters;
    exports com.telenav.mesakit.navigation.routing.bidijkstra;
    exports com.telenav.mesakit.navigation.routing.limiters;
    exports com.telenav.mesakit.navigation.routing.cost;
    exports com.telenav.mesakit.navigation.routing.cost.operators;
    exports com.telenav.mesakit.navigation.routing.cost.functions;
    exports com.telenav.mesakit.navigation.routing.cost.functions.heuristic;
    exports com.telenav.mesakit.navigation.routing.debuggers;
    exports com.telenav.mesakit.navigation.routing.dijkstra;
}
