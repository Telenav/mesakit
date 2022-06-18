open module mesakit.map.utilities.geojson
{
    // MesaKit
    requires transitive mesakit.map.geography;

    // KivaKit
    requires transitive kivakit.resource;

    // JSON
    requires com.google.gson;

    // Exports
    exports com.telenav.mesakit.map.utilities.geojson;
}
