package com.telenav.mesakit.map.road.name.parser.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapRoadNameParserProject extends Project
{
    private static final Lazy<MapRoadNameParserProject> singleton = Lazy.of(MapRoadNameParserProject::new);

    public static MapRoadNameParserProject get()
    {
        return singleton.get();
    }

    protected MapRoadNameParserProject()
    {
    }
}
