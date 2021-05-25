package com.telenav.mesakit.map.measurements.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * Project class for mesakit-map-measurements
 *
 * @author jonathanl (shibo)
 */
public class MapMeasurementsProject extends Project
{
    private static final Lazy<MapMeasurementsProject> project = Lazy.of(MapMeasurementsProject::new);

    public static MapMeasurementsProject get()
    {
        return project.get();
    }

    protected MapMeasurementsProject()
    {
    }
}
