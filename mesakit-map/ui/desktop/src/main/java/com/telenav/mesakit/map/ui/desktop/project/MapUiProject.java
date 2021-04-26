package com.telenav.mesakit.map.ui.desktop.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapUiProject extends Project
{
    private static final Lazy<MapUiProject> project = Lazy.of(MapUiProject::new);

    public static MapUiProject get()
    {
        return project.get();
    }

    protected MapUiProject()
    {
    }
}
