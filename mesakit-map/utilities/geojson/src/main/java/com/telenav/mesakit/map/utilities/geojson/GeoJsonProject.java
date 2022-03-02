package com.telenav.mesakit.map.utilities.geojson;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

/**
 * @author jonathanl (shibo)
 */
public class GeoJsonProject extends Project
{
    private static final Lazy<GeoJsonProject> project = Lazy.of(GeoJsonProject::new);

    public static GeoJsonProject get()
    {
        return project.get();
    }

    protected GeoJsonProject()
    {
    }
}
