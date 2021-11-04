open module mesakit.map.cutter
{
    // MesaKit
    requires transitive mesakit.map.region;

    // KivaKit
    requires kivakit.primitive.collections;
    requires kivakit.test;

    // OSM
    requires osmosis.core;

    // Exports
    exports com.telenav.mesakit.map.cutter;
    exports com.telenav.mesakit.map.cutter.cuts;
}
