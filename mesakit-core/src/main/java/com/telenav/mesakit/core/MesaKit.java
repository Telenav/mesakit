package com.telenav.mesakit.core;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.core.vm.SystemProperties;
import com.telenav.kivakit.filesystem.Folder;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * @author jonathanl (shibo)
 */
public class MesaKit extends Project
{
    private static final Lazy<MesaKit> mesakit = Lazy.of(MesaKit::new);

    public static MesaKit get()
    {
        return mesakit.get();
    }

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
        var home = SystemProperties.property("MESAKIT_HOME");
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
