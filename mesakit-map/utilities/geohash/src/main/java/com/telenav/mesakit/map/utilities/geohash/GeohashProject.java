package com.telenav.mesakit.map.utilities.geohash;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

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
