package com.telenav.mesakit.map.ui.desktop;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

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
