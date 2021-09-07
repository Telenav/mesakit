package com.telenav.mesakit.map.utilities.geohash;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class GeohashProject extends Project
{
    private static final Lazy<GeohashProject> project = Lazy.of(GeohashProject::new);

    public static GeohashProject get()
    {
        return project.get();
    }

    protected GeohashProject()
    {
    }
}
