open module mesakit.map.ui.debug
{
    requires transitive mesakit.core;
    requires transitive mesakit.map.geography;
    requires transitive mesakit.map.ui.desktop;

    requires transitive kivakit.core.network.http;

    exports com.telenav.mesakit.map.ui.debug;
    exports com.telenav.mesakit.map.ui.debug.indexing.rtree;
    exports com.telenav.mesakit.map.ui.debug.indexing.polygon;
    exports com.telenav.mesakit.map.ui.debug.viewer.empty;
    exports com.telenav.mesakit.map.ui.debug.viewer.desktop;
    exports com.telenav.mesakit.map.ui.debug.viewer;
}
