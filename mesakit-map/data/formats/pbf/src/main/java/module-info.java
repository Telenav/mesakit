open module mesakit.data.formats.pbf
{
    requires transitive mesakit.map.data.library;

    requires transitive kivakit.data.formats.library;
    requires transitive kivakit.data.compression;
    requires transitive kivakit.primitive.collections;
    requires kivakit.test;

    requires osmosis.core;
    requires osmosis.osm.binary;
    requires osmosis.pbf;

    exports com.telenav.mesakit.map.data.formats.pbf.model.entities;
    exports com.telenav.mesakit.map.data.formats.pbf.model.extractors;
    exports com.telenav.mesakit.map.data.formats.pbf.model.identifiers;
    exports com.telenav.mesakit.map.data.formats.pbf.model.metadata;
    exports com.telenav.mesakit.map.data.formats.pbf.model.tags.compression;
    exports com.telenav.mesakit.map.data.formats.pbf.model.tags;
    exports com.telenav.mesakit.map.data.formats.pbf.osm;
    exports com.telenav.mesakit.map.data.formats.pbf.processing.filters.all;
    exports com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm;
    exports com.telenav.mesakit.map.data.formats.pbf.processing.filters;
    exports com.telenav.mesakit.map.data.formats.pbf.processing.readers;
    exports com.telenav.mesakit.map.data.formats.pbf.processing.writers;
    exports com.telenav.mesakit.map.data.formats.pbf.processing;
    exports com.telenav.mesakit.map.data.formats.pbf.project;
    exports com.telenav.mesakit.map.data.formats.pbf;
}
