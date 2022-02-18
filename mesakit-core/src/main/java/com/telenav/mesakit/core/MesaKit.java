package com.telenav.mesakit.core;

import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.project.Project;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureEqual;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

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
        if (!projectVersion().equals(Version.parse(this, "0.9.9-SNAPSHOT")))
        {
            Message.println("oops");
        }
        return mesakitRootCacheFolder().folder(projectVersion().toString()).mkdirs();
    }

    public Folder mesakitExtensionsHome()
    {
        return mesakitHome().parent().folder("mesakit-extensions");
    }

    public Folder mesakitHome()
    {
        var home = JavaVirtualMachine.property("MESAKIT_HOME");
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
