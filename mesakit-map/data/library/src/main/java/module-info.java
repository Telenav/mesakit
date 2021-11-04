open module mesakit.map.data.library
{
    requires transitive kivakit.resource;
    requires transitive kivakit.math;
    requires transitive kivakit.data.compression;

    exports com.telenav.mesakit.map.data.formats.library;
    exports com.telenav.mesakit.map.data.formats.library.map.identifiers;
}
