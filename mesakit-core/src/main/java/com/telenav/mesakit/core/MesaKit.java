package com.telenav.mesakit.core;

import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.core.project.ProjectTrait;
import com.telenav.kivakit.filesystem.Folder;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * This class defines a KivaKit {@link Project}. It cannot be constructed with the new operator since it has a private
 * constructor. To access the singleton instance of this class, call {@link Project#resolveProject(Class)}, or use
 * {@link ProjectTrait#project(Class)}.
 *
 * @author jonathanl (shibo)
 */
public class MesaKit extends Project
{
    public Folder mesakitAllVersionsCacheFolder()
    {
        return mesakitRootCacheFolder().folder("all-versions");
    }

    public Folder mesakitCacheFolder()
    {
        return mesakitRootCacheFolder().folder(projectVersion().toString()).mkdirs();
    }

    public Folder mesakitExtensionsHome()
    {
        return mesakitHome().parent().folder("mesakit-extensions");
    }

    public Folder mesakitHome()
    {
        var home = systemProperty("MESAKIT_HOME");
        if (home != null)
        {
            return Folder.parse(this, home);
        }
        return fail("Cannot find MesaKit home folder");
    }

    public Folder mesakitRootCacheFolder()
    {
        return Folder.userHome().folder(".mesakit");
    }
}
