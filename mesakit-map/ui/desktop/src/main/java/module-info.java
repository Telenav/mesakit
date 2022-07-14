open module mesakit.map.ui.desktop
{
    requires transitive mesakit.core;
    requires transitive mesakit.map.geography;

    requires transitive kivakit.network.http;
    requires transitive kivakit.ui.desktop;
    requires java.net.http;

    exports com.telenav.mesakit.map.ui.desktop.graphics.canvas;
    exports com.telenav.mesakit.map.ui.desktop.graphics.drawables;
    exports com.telenav.mesakit.map.ui.desktop.theme;
    exports com.telenav.mesakit.map.ui.desktop.theme.shapes;
    exports com.telenav.mesakit.map.ui.desktop.tiles;
    exports com.telenav.mesakit.map.ui.desktop.viewer;
    exports com.telenav.mesakit.map.ui.desktop.viewer.desktop;
    exports com.telenav.mesakit.map.ui.desktop.viewer.empty;
    exports com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections;
    exports com.telenav.mesakit.map.ui.desktop.graphics.style;
}
