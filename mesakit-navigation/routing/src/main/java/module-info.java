open module tdk.navigation.routing
{
    requires transitive tdk.graph.world;
    requires transitive tdk.map.ui;
    requires transitive tdk.map.region;

    exports com.telenav.tdk.navigation.routing;
    exports com.telenav.tdk.navigation.routing.promoters;
    exports com.telenav.tdk.navigation.routing.bidijkstra;
    exports com.telenav.tdk.navigation.routing.limiters;
    exports com.telenav.tdk.navigation.routing.cost;
    exports com.telenav.tdk.navigation.routing.cost.operators;
    exports com.telenav.tdk.navigation.routing.cost.functions;
    exports com.telenav.tdk.navigation.routing.cost.functions.heuristic;
    exports com.telenav.tdk.navigation.routing.debuggers;
    exports com.telenav.tdk.navigation.routing.dijkstra;
}
