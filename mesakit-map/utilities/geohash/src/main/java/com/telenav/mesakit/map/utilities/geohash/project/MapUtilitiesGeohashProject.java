package com.telenav.mesakit.map.utilities.geohash.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUtilitiesGeohashProject extends Project
{
    private static final Lazy<MapUtilitiesGeohashProject> singleton = Lazy.of(MapUtilitiesGeohashProject::new);

    public static MapUtilitiesGeohashProject get()
    {
        return singleton.get();
    }

    protected MapUtilitiesGeohashProject()
    {
    }
}
