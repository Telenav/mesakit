package com.telenav.mesakit.map.overpass;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

/**
 * @author jonathanl (shibo)
 */
public class OverpassProject extends Project
{
    private static final Lazy<OverpassProject> project = Lazy.of(OverpassProject::new);

    public static OverpassProject get()
    {
        return project.get();
    }

    protected OverpassProject()
    {
    }
}
