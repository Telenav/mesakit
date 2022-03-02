package com.telenav.mesakit.map.ui.desktop;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapDesktopUiProject extends Project
{
    private static final Lazy<MapDesktopUiProject> project = Lazy.of(MapDesktopUiProject::new);

    public static MapDesktopUiProject get()
    {
        return project.get();
    }

    protected MapDesktopUiProject()
    {
    }
}
