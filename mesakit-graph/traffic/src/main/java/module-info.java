open module tdk.graph.traffic
{
    requires transitive tdk.data.formats.csv;
    requires transitive tdk.map.region;
    requires transitive tdk.map.road.model;

    exports com.telenav.tdk.graph.traffic;
    exports com.telenav.tdk.graph.traffic.roadsection;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.tomtom;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.osm;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.ngx;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.legacy;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.telenav;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.navteq;
    exports com.telenav.tdk.graph.traffic.roadsection.codings.tmc;
    exports com.telenav.tdk.graph.traffic.roadsection.loaders.csv;
    exports com.telenav.tdk.graph.traffic.project;
    exports com.telenav.tdk.graph.traffic.historical;
    exports com.telenav.tdk.graph.traffic.extractors;
}
