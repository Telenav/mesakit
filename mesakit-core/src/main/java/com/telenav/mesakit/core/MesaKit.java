package com.telenav.mesakit.core;

import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.project.Project;

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

    public Folder cacheFolder()
    {
        return mesakitFolder().folder(version().toString()).mkdirs();
    }

    public Folder mesakitAllVersionsFolder()
    {
        return mesakitFolder().folder("all-versions");
    }

    public Folder mesakitFolder()
    {
        return Folder.userHome().folder(".mesakit");
    }
}
