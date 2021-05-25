package com.telenav.mesakit.map.road.name.parser.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapRoadNameParserProject extends Project
{
    private static final Lazy<MapRoadNameParserProject> project = Lazy.of(MapRoadNameParserProject::new);

    public static MapRoadNameParserProject get()
    {
        return project.get();
    }

    protected MapRoadNameParserProject()
    {
    }
}
