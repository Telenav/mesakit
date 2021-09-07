package com.telenav.mesakit.map.measurements;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * Project class for mesakit-map-measurements
 *
 * @author jonathanl (shibo)
 */
public class MeasurementsProject extends Project
{
    private static final Lazy<MeasurementsProject> project = Lazy.of(MeasurementsProject::new);

    public static MeasurementsProject get()
    {
        return project.get();
    }

    protected MeasurementsProject()
    {
    }
}
