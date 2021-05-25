package com.telenav.mesakit.map.overpass.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapOverpassProject extends Project
{
    private static final Lazy<MapOverpassProject> project = Lazy.of(MapOverpassProject::new);

    public static MapOverpassProject get()
    {
        return project.get();
    }

    protected MapOverpassProject()
    {
    }
}
