package com.telenav.aonia.map.utilities.grid.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUtilitiesGridProject extends Project
{
    private static final Lazy<MapUtilitiesGridProject> singleton = Lazy.of(MapUtilitiesGridProject::new);

    public static MapUtilitiesGridProject get()
    {
        return singleton.get();
    }

    protected MapUtilitiesGridProject()
    {
    }
}
