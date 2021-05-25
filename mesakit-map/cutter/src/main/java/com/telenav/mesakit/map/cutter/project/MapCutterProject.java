package com.telenav.mesakit.map.cutter.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapCutterProject extends Project
{
    private static final Lazy<MapCutterProject> project = Lazy.of(MapCutterProject::new);

    public static MapCutterProject get()
    {
        return project.get();
    }

    protected MapCutterProject()
    {
    }
}
