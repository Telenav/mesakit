package com.telenav.aonia.map.measurements.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * Project class for aonia-map-measurements
 *
 * @author jonathanl (shibo)
 */
public class MapMeasurementsProject extends Project
{
    private static final Lazy<MapMeasurementsProject> singleton = Lazy.of(MapMeasurementsProject::new);

    public static MapMeasurementsProject get()
    {
        return singleton.get();
    }

    protected MapMeasurementsProject()
    {
    }
}
