package com.telenav.mesakit.map.data.formats.pbf.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class DataFormatsPbfProject extends Project
{
    private static final Lazy<DataFormatsPbfProject> project = Lazy.of(DataFormatsPbfProject::new);

    public static DataFormatsPbfProject get()
    {
        return project.get();
    }

    protected DataFormatsPbfProject()
    {
    }
}
