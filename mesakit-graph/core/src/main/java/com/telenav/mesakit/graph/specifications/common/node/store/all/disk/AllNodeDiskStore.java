////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.specifications.common.node.store.all.disk;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.io.IO;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.resource.compression.archive.ZipEntry;
import com.telenav.mesakit.graph.io.archive.GraphArchive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AllNodeDiskStore
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private Boolean containsData;

    private final Folder folder;

    private final GraphArchive archive;

    private final Map<AllNodeDiskCell, DataOutputStream> output = new HashMap<>();

    protected AllNodeDiskStore(Folder folder)
    {
        this.folder = folder;
        archive = null;
        folder.mkdirs();
        folder.clearAll();
    }

    protected AllNodeDiskStore(GraphArchive archive)
    {
        folder = null;
        this.archive = archive;
    }

    public Iterable<AllNodeDiskCell> DiskCells()
    {
        return output.keySet();
    }

    public GraphArchive archive()
    {
        return archive;
    }

    public boolean containsData()
    {
        if (containsData == null)
        {
            if (archive() != null)
            {
                try (var zip = archive().zip())
                {
                    containsData = !zip.entries(Pattern.compile(name() + ".*")).isEmpty();
                }
            }
            else
            {
                return !folder.files().matching(name -> name.baseName().startsWith(name())).isEmpty();
            }
        }
        return containsData;
    }

    public void delete()
    {
        for (var DiskCell : DiskCells())
        {
            fileForDiskCell(DiskCell).delete();
        }
        folder.delete();
    }

    public InputStream in(AllNodeDiskCell DiskCell)
    {
        return fileForDiskCell(DiskCell).openForReading();
    }

    public void saveTo(GraphArchive archive)
    {
        LOGGER.information("Saving store $ to $", folder, archive);
        close();
        for (var DiskCell : DiskCells())
        {
            archive.zip().save(DiskCell.toFileString(name()), fileForDiskCell(DiskCell));
        }
    }

    @Override
    public String toString()
    {
        return "[" + getClass().getSimpleName() + " root = " + folder + ", archive = " + archive + "]";
    }

    protected ZipEntry entry(AllNodeDiskCell DiskCell)
    {
        var file = DiskCell.toFileString(name());
        var entry = archive().zip().entry(file);
        if (entry == null)
        {
            throw new IllegalStateException(file + " is not available in " + this);
        }
        return entry;
    }

    protected DataInputStream input(AllNodeDiskCell DiskCell)
    {
        return new DataInputStream(in(DiskCell));
    }

    protected abstract String name();

    protected DataOutputStream output(AllNodeDiskCell DiskCell)
    {
        var out = output.get(DiskCell);
        if (out == null)
        {
            out = new DataOutputStream(fileForDiskCell(DiskCell).openForWriting());
            output.put(DiskCell, out);
        }
        return out;
    }

    private void close()
    {
        for (var out : output.values())
        {
            IO.flush(out);
            IO.close(out);
        }
    }

    private File fileForDiskCell(AllNodeDiskCell DiskCell)
    {
        return folder.file(DiskCell.toFileString(name()));
    }
}
