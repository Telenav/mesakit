package com.telenav.mesakit.map.utilities.grid;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.coreproject.Project;

/**
 * @author jonathanl (shibo)
 */
public class GridProject extends Project
{
    private static final Lazy<GridProject> project = Lazy.of(GridProject::new);

    public static GridProject get()
    {
        return project.get();
    }

    protected GridProject()
    {
    }
}
