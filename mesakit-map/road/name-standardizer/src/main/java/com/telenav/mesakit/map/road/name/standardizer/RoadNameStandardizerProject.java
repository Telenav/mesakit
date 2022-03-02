package com.telenav.mesakit.map.road.name.standardizer;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

/**
 * @author jonathanl (shibo)
 */
public class RoadNameStandardizerProject extends Project
{
    private static final Lazy<RoadNameStandardizerProject> project = Lazy.of(RoadNameStandardizerProject::new);

    public static RoadNameStandardizerProject get()
    {
        return project.get();
    }

    protected RoadNameStandardizerProject()
    {
    }
}
