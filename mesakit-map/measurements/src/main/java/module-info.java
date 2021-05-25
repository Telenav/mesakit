open module mesakit.map.measurements
{
    requires transitive kivakit.commandline;
    requires transitive kivakit.test;
    requires transitive kivakit.math;

    exports com.telenav.mesakit.map.measurements.geographic;
    exports com.telenav.mesakit.map.measurements.motion.speeds;
    exports com.telenav.mesakit.map.measurements.motion;
    exports com.telenav.mesakit.map.measurements.project;
}
