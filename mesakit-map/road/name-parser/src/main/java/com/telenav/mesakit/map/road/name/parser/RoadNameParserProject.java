package com.telenav.mesakit.map.road.name.parser;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class RoadNameParserProject extends Project
{
    private static final Lazy<RoadNameParserProject> project = Lazy.of(RoadNameParserProject::new);

    public static RoadNameParserProject get()
    {
        return project.get();
    }

    protected RoadNameParserProject()
    {
    }
}
