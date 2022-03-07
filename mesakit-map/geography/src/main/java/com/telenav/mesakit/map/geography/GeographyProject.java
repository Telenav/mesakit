package com.telenav.mesakit.map.geography;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.serialization.kryo.CoreKryoTypes;
import com.telenav.kivakit.serialization.kryo.KryoTypes;
import com.telenav.mesakit.map.geography.project.GeographyKryoTypes;
import com.telenav.mesakit.map.measurements.project.MeasurementsKryoTypes;

/**
 * @author jonathanl (shibo)
 */
public class GeographyProject extends Project
{
    private static final Lazy<GeographyProject> project = Lazy.of(GeographyProject::new);

    private static final KryoTypes KRYO_TYPES = new GeographyKryoTypes()
            .mergedWith(new MeasurementsKryoTypes())
            .mergedWith(new CoreKryoTypes());

    public static GeographyProject get()
    {
        return project.get();
    }

    protected GeographyProject()
    {
    }

    public KryoTypes kryoTypes()
    {
        return KRYO_TYPES;
    }
}
