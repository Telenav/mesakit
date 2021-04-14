package com.telenav.aonia.map.utilities.geojson.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUtilitiesGeoJsonProject extends Project
{
    private static final Lazy<MapUtilitiesGeoJsonProject> singleton = Lazy.of(MapUtilitiesGeoJsonProject::new);

    public static MapUtilitiesGeoJsonProject get()
    {
        return singleton.get();
    }

    protected MapUtilitiesGeoJsonProject()
    {
    }
}
