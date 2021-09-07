package com.telenav.mesakit.map.utilities.geojson;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

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
