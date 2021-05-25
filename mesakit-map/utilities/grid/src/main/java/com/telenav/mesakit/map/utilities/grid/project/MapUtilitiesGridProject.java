package com.telenav.mesakit.map.utilities.grid.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUtilitiesGridProject extends Project
{
    private static final Lazy<MapUtilitiesGridProject> project = Lazy.of(MapUtilitiesGridProject::new);

    public static MapUtilitiesGridProject get()
    {
        return project.get();
    }

    protected MapUtilitiesGridProject()
    {
    }
}
