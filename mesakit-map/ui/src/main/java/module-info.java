open module mesakit.map.ui
{
    requires transitive mesakit.map.geography;

    requires transitive kivakit.core.network.http;
    requires transitive kivakit.ui.swing;

    exports com.telenav.mesakit.map.ui.swing.debug.debuggers.indexing.rtree;
    exports com.telenav.mesakit.map.ui.swing.debug.viewables;
    exports com.telenav.mesakit.map.ui.swing.debug.viewer.empty;
    exports com.telenav.mesakit.map.ui.swing.debug.viewer.swing;
    exports com.telenav.mesakit.map.ui.swing.debug.viewer;
    exports com.telenav.mesakit.map.ui.swing.debug;
    exports com.telenav.mesakit.map.ui.swing.map.coordinates.mappers;
    exports com.telenav.mesakit.map.ui.swing.map.coordinates.projections;
    exports com.telenav.mesakit.map.ui.swing.map.graphics.canvas;
    exports com.telenav.mesakit.map.ui.swing.map.graphics.drawables;
    exports com.telenav.mesakit.map.ui.swing.map.theme;
    exports com.telenav.mesakit.map.ui.swing.map.tiles;
}
