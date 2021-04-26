package com.telenav.mesakit.map.road.name.standardizer.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapRoadNameStandardizerProject extends Project
{
    private static final Lazy<MapRoadNameStandardizerProject> project = Lazy.of(MapRoadNameStandardizerProject::new);

    public static MapRoadNameStandardizerProject get()
    {
        return project.get();
    }

    protected MapRoadNameStandardizerProject()
    {
    }
}
