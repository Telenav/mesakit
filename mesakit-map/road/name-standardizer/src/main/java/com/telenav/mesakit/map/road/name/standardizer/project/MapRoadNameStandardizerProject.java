package com.telenav.mesakit.map.road.name.standardizer.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapRoadNameStandardizerProject extends Project
{
    private static final Lazy<MapRoadNameStandardizerProject> singleton = Lazy.of(MapRoadNameStandardizerProject::new);

    public static MapRoadNameStandardizerProject get()
    {
        return singleton.get();
    }

    protected MapRoadNameStandardizerProject()
    {
    }
}
