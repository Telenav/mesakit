package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

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
