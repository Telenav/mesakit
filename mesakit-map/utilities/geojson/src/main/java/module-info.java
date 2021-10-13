open module mesakit.map.utilities.geojson
{
    // MesaKit
    requires transitive mesakit.map.geography;

    // KivaKit
    requires transitive kivakit.resource;
    requires kivakit.test;

    // JSON
    requires gson;

    // Exports
    exports com.telenav.mesakit.map.utilities.geojson;
}
