open module mesakit.map.overpass
{
    // MesaKit
    requires transitive mesakit.map.geography;

    // KivaKit
    requires transitive kivakit.network.http;
    requires transitive kivakit.component;

    requires osmosis.xml;
    requires osmosis.core;
    requires osmosis.osm.binary;
    requires osmosis.pbf;

    exports com.telenav.mesakit.map.overpass;
}
