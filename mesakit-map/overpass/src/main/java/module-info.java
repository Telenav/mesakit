open module mesakit.map.overpass
{
    requires transitive mesakit.map.geography;
    requires transitive kivakit.network.http;

    requires osmosis.xml;
    requires osmosis.core;
    requires osmosis.osm.binary;
    requires osmosis.pbf;

    exports com.telenav.mesakit.map.overpass;
}
