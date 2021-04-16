package com.telenav.mesakit.map.data.formats.pbf.project;

import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class DataFormatsPbfProject extends Project
{
    private static final Lazy<DataFormatsPbfProject> singleton = Lazy.of(DataFormatsPbfProject::new);

    public static DataFormatsPbfProject get()
    {
        return singleton.get();
    }

    protected DataFormatsPbfProject()
    {
    }
}
