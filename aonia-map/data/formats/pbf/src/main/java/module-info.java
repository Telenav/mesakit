open module aonia.data.formats.pbf
{
    requires transitive aonia.map.data.library;

    requires transitive kivakit.data.formats.library;
    requires transitive kivakit.data.compression;

    requires transitive osmosis.core;
    requires transitive osmosis.osm.binary;
    requires transitive osmosis.pbf;

    exports com.telenav.aonia.map.data.formats.pbf.model.entities;
    exports com.telenav.aonia.map.data.formats.pbf.model.extractors;
    exports com.telenav.aonia.map.data.formats.pbf.model.identifiers;
    exports com.telenav.aonia.map.data.formats.pbf.model.metadata;
    exports com.telenav.aonia.map.data.formats.pbf.model.tags.compression;
    exports com.telenav.aonia.map.data.formats.pbf.model.tags;
    exports com.telenav.aonia.map.data.formats.pbf.osm;
    exports com.telenav.aonia.map.data.formats.pbf.processing.filters.all;
    exports com.telenav.aonia.map.data.formats.pbf.processing.filters.navteam;
    exports com.telenav.aonia.map.data.formats.pbf.processing.filters.osm;
    exports com.telenav.aonia.map.data.formats.pbf.processing.filters.osmteam;
    exports com.telenav.aonia.map.data.formats.pbf.processing.filters.unidb;
    exports com.telenav.aonia.map.data.formats.pbf.processing.filters;
    exports com.telenav.aonia.map.data.formats.pbf.processing.readers;
    exports com.telenav.aonia.map.data.formats.pbf.processing.writers;
    exports com.telenav.aonia.map.data.formats.pbf.processing;
    exports com.telenav.aonia.map.data.formats.pbf.project;
}
