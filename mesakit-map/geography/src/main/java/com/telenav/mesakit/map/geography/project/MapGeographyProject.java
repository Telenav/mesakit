package com.telenav.mesakit.map.geography.project;

import com.telenav.kivakit.core.collections.project.CoreCollectionsKryoTypes;
import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;
import com.telenav.kivakit.core.serialization.kryo.CoreKernelKryoTypes;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;
import com.telenav.mesakit.map.measurements.project.MapMeasurementsKryoTypes;

/**
 * @author jonathanl (shibo)
 */
public class MapGeographyProject extends Project
{
    private static final Lazy<MapGeographyProject> project = Lazy.of(MapGeographyProject::new);

    private static final KryoTypes KRYO_TYPES = new MapGeographyKryoTypes()
            .mergedWith(new MapMeasurementsKryoTypes())
            .mergedWith(new CoreCollectionsKryoTypes())
            .mergedWith(new CoreKernelKryoTypes());

    public static MapGeographyProject get()
    {
        return project.get();
    }

    protected MapGeographyProject()
    {
    }

    public KryoTypes kryoTypes()
    {
        return KRYO_TYPES;
    }
}
