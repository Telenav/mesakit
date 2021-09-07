package com.telenav.mesakit.map.data.formats.pbf;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

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
