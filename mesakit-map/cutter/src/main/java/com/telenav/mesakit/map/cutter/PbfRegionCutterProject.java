package com.telenav.mesakit.map.cutter;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class PbfRegionCutterProject extends Project
{
    private static final Lazy<PbfRegionCutterProject> project = Lazy.of(PbfRegionCutterProject::new);

    public static PbfRegionCutterProject get()
    {
        return project.get();
    }

    protected PbfRegionCutterProject()
    {
    }
}
