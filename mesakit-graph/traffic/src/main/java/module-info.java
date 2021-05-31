open module mesakit.graph.traffic
{
    requires transitive kivakit.data.formats.csv;
    requires transitive kivakit.configuration;
    requires transitive mesakit.map.region;
    requires transitive mesakit.map.road.model;

    exports com.telenav.kivakit.graph.traffic;
    exports com.telenav.kivakit.graph.traffic.roadsection;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.tomtom;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.osm;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.ngx;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.legacy;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.telenav;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.navteq;
    exports com.telenav.kivakit.graph.traffic.roadsection.codings.tmc;
    exports com.telenav.kivakit.graph.traffic.roadsection.loaders.csv;
    exports com.telenav.kivakit.graph.traffic.project;
    exports com.telenav.kivakit.graph.traffic.historical;
    exports com.telenav.kivakit.graph.traffic.extractors;
    exports com.telenav.mesakit.graph.traffic.roadsection;
    exports com.telenav.mesakit.graph.traffic.historical;
    exports com.telenav.mesakit.graph.traffic.project;
    exports com.telenav.mesakit.graph.traffic.extractors;
}
