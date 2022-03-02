package com.telenav.mesakit.map.cutter;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

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
