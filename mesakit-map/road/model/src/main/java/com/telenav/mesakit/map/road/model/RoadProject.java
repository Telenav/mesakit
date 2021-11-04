package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class RoadProject extends Project
{
    private static final Lazy<RoadProject> project = Lazy.of(RoadProject::new);

    public static RoadProject get()
    {
        return project.get();
    }

    protected RoadProject()
    {
    }
}
