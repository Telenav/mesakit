open module mesakit.map.ui.debug
{
    requires transitive mesakit.map.geography;
    requires transitive mesakit.map.ui.desktop;

    exports com.telenav.mesakit.map.ui.debug.debuggers;
    exports com.telenav.mesakit.map.ui.debug.theme;
    exports com.telenav.mesakit.map.ui.debug;
}
