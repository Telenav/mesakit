package com.telenav.mesakit.map.road.name.parser;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

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
