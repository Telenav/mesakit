package com.telenav.mesakit.map.geography.project;

import com.telenav.mesakit.map.measurements.project.MapMeasurementsKryoTypes;
import com.telenav.kivakit.core.collections.project.CoreCollectionsKryoTypes;
import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;
import com.telenav.kivakit.core.serialization.kryo.CoreKernelKryoTypes;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;

/**
 * @author jonathanl (shibo)
 */
public class MapGeographyProject extends Project
{
    private static final Lazy<MapGeographyProject> singleton = Lazy.of(MapGeographyProject::new);

    private static final KryoTypes KRYO_TYPES = new MapGeographyKryoTypes()
            .mergedWith(new MapMeasurementsKryoTypes())
            .mergedWith(new CoreCollectionsKryoTypes())
            .mergedWith(new CoreKernelKryoTypes());

    public static MapGeographyProject get()
    {
        return singleton.get();
    }

    protected MapGeographyProject()
    {
    }

    public KryoTypes kryoTypes()
    {
        return KRYO_TYPES;
    }
}
