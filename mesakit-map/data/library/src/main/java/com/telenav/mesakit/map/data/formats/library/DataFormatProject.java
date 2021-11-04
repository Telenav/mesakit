package com.telenav.mesakit.map.data.formats.library;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class DataFormatProject extends Project
{
    private static final Lazy<DataFormatProject> project = Lazy.of(DataFormatProject::new);

    public static DataFormatProject get()
    {
        return project.get();
    }

    protected DataFormatProject()
    {
    }
}
