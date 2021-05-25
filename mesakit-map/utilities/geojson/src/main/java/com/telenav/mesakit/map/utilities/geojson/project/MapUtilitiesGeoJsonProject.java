package com.telenav.mesakit.map.utilities.geojson.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUtilitiesGeoJsonProject extends Project
{
    private static final Lazy<MapUtilitiesGeoJsonProject> project = Lazy.of(MapUtilitiesGeoJsonProject::new);

    public static MapUtilitiesGeoJsonProject get()
    {
        return project.get();
    }

    protected MapUtilitiesGeoJsonProject()
    {
    }
}
