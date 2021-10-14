open module mesakit.map.measurements
{
    requires kivakit.test;
    requires transitive kivakit.commandline;
    requires transitive kivakit.serialization.kryo;
    requires kivakit.math;
    requires kivakit.collections;

    exports com.telenav.mesakit.map.measurements.geographic;
    exports com.telenav.mesakit.map.measurements.motion.speeds;
    exports com.telenav.mesakit.map.measurements.motion;
    exports com.telenav.mesakit.map.measurements.project;
    exports com.telenav.mesakit.map.measurements;
}
