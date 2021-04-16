package com.telenav.mesakit.map.overpass.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapOverpassProject extends Project
{
    private static final Lazy<MapOverpassProject> singleton = Lazy.of(MapOverpassProject::new);

    public static MapOverpassProject get()
    {
        return singleton.get();
    }

    protected MapOverpassProject()
    {
    }
}
