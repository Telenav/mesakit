open module mesakit.map.overpass
{
    requires transitive mesakit.map.geography;
    requires transitive kivakit.network.http;

    requires transitive osmosis.xml;
    requires transitive osmosis.core;
    requires transitive osmosis.osm.binary;
    requires transitive osmosis.pbf;

    exports com.telenav.mesakit.map.overpass;
}
