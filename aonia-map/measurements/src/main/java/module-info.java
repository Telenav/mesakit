open module aonia.map.measurements
{
    requires transitive kivakit.core.commandline;
    requires transitive kivakit.core.test;
    requires transitive kivakit.math;

    exports com.telenav.aonia.map.measurements.geographic;
    exports com.telenav.aonia.map.measurements.motion.speeds;
    exports com.telenav.aonia.map.measurements.motion;
    exports com.telenav.aonia.map.measurements.project;
}
