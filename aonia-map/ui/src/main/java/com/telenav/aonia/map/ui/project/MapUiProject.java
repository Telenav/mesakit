package com.telenav.aonia.map.ui.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUiProject extends Project
{
    private static final Lazy<MapUiProject> singleton = Lazy.of(MapUiProject::new);

    public static MapUiProject get()
    {
        return singleton.get();
    }

    protected MapUiProject()
    {
    }
}
