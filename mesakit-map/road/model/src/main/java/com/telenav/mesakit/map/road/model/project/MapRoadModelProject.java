package com.telenav.mesakit.map.road.model.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapRoadModelProject extends Project
{
    private static final Lazy<MapRoadModelProject> project = Lazy.of(MapRoadModelProject::new);

    public static MapRoadModelProject get()
    {
        return project.get();
    }

    protected MapRoadModelProject()
    {
    }
}
