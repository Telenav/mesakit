package com.telenav.mesakit.map.road.name.standardizer;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

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
