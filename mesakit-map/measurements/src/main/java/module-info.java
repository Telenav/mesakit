open module mesakit.map.measurements
{
    requires transitive kivakit.commandline;
    requires kivakit.math;
    requires kivakit.collections;
    requires kivakit.serialization.kryo;

    exports com.telenav.mesakit.map.measurements.geographic;
    exports com.telenav.mesakit.map.measurements.motion.speeds;
    exports com.telenav.mesakit.map.measurements.motion;
    exports com.telenav.mesakit.map.measurements;
}
