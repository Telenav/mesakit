open module aonia.map.data.library
{
    requires transitive kivakit.core.resource;
    requires transitive kivakit.math;
    requires transitive kivakit.data.compression;

    exports com.telenav.aonia.map.data.formats.library;
    exports com.telenav.aonia.map.data.formats.library.map.identifiers;
}
