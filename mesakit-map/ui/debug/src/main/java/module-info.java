open module mesakit.map.ui.debug
{
    requires transitive mesakit.core;
    requires transitive mesakit.map.geography;
    requires transitive mesakit.map.ui.desktop;

    requires transitive kivakit.network.http;

    exports com.telenav.mesakit.map.ui.debug.debuggers;
    exports com.telenav.mesakit.map.ui.debug.theme;
    exports com.telenav.mesakit.map.ui.debug;
}
