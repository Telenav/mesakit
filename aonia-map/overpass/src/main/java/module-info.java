open module aonia.map.overpass
{
    requires transitive aonia.map.geography;
    requires transitive kivakit.core.network.http;

    requires transitive osmosis.xml;
    requires transitive osmosis.core;
    requires transitive osmosis.osm.binary;
    requires transitive osmosis.pbf;

    exports com.telenav.aonia.map.overpass;
}
