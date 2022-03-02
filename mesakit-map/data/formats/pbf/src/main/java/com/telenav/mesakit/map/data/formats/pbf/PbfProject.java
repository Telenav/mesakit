package com.telenav.mesakit.map.data.formats.pbf;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

/**
 * @author jonathanl (shibo)
 */
public class PbfProject extends Project
{
    private static final Lazy<PbfProject> project = Lazy.of(PbfProject::new);

    public static PbfProject get()
    {
        return project.get();
    }

    protected PbfProject()
    {
    }
}
