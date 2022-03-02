package com.telenav.mesakit.map.data.formats.library;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

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
