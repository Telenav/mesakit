package com.telenav.mesakit.map.data.formats.library.project;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.project.Project;

/**
 * @author jonathanl (shibo)
 */
public class DataFormatsLibraryProject extends Project
{
    private static final Lazy<DataFormatsLibraryProject> project = Lazy.of(DataFormatsLibraryProject::new);

    public static DataFormatsLibraryProject get()
    {
        return project.get();
    }

    protected DataFormatsLibraryProject()
    {
    }
}
