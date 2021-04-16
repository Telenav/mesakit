package com.telenav.mesakit.map.road.model.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapRoadModelProject extends Project
{
    private static final Lazy<MapRoadModelProject> singleton = Lazy.of(MapRoadModelProject::new);

    public static MapRoadModelProject get()
    {
        return singleton.get();
    }

    protected MapRoadModelProject()
    {
    }
}
