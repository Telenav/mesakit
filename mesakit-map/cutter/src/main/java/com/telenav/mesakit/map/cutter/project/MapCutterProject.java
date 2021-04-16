package com.telenav.mesakit.map.cutter.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class MapCutterProject extends Project
{
    private static final Lazy<MapCutterProject> singleton = Lazy.of(MapCutterProject::new);

    public static MapCutterProject get()
    {
        return singleton.get();
    }

    protected MapCutterProject()
    {
    }
}
