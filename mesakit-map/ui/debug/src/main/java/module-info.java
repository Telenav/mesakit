open module mesakit.map.ui.debug
{
    requires transitive mesakit.core;
    requires transitive mesakit.map.geography;

    requires transitive kivakit.core.network.http;
    requires transitive kivakit.ui.desktop;

    exports com.telenav.mesakit.map.ui.desktop.debug.debuggers.indexing.rtree;
    exports com.telenav.mesakit.map.ui.desktop.debug.viewer.empty;
    exports com.telenav.mesakit.map.ui.desktop.debug.viewer.swing;
    exports com.telenav.mesakit.map.ui.desktop.debug.viewer;
    exports com.telenav.mesakit.map.ui.desktop.debug;
}
