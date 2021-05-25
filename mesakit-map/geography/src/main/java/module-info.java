open module mesakit.map.geography
{
    requires transitive java.desktop;

    requires transitive kivakit.data.compression;
    requires transitive kivakit.resource;
    requires transitive kivakit.math;

    requires transitive mesakit.map.measurements;

    exports com.telenav.mesakit.map.geography.indexing.polygon;
    exports com.telenav.mesakit.map.geography.indexing.quadtree;
    exports com.telenav.mesakit.map.geography.indexing.rtree;
    exports com.telenav.mesakit.map.geography.indexing.segment;
    exports com.telenav.mesakit.map.geography.shape.polyline.compression.huffman;
    exports com.telenav.mesakit.map.geography.shape.polyline.compression.differential;
    exports com.telenav.mesakit.map.geography.shape.polyline;
    exports com.telenav.mesakit.map.geography.project;
    exports com.telenav.mesakit.map.geography.projection;
    exports com.telenav.mesakit.map.geography.shape.rectangle;
    exports com.telenav.mesakit.map.geography.shape.segment;
    exports com.telenav.mesakit.map.geography.shape;
    exports com.telenav.mesakit.map.geography;
    exports com.telenav.mesakit.map.geography.projection.projections;
}
