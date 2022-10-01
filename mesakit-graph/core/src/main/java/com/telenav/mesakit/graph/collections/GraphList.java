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

package com.telenav.mesakit.graph.collections;

import com.telenav.kivakit.filesystem.FileList;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.io.archive.GraphArchive;

import java.util.AbstractList;

import static com.telenav.kivakit.resource.compression.archive.ZipArchive.AccessMode.READ;

public class GraphList extends AbstractList<Graph>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final FileList files;

    public GraphList(FileList files)
    {
        this.files = files;
    }

    @SuppressWarnings("resource")
    @Override
    public Graph get(int index)
    {
        var resource = files.get(index);
        try
        {
            return new GraphArchive(LOGGER, resource, READ, ProgressReporter.none()).load(LOGGER);
        }
        catch (Exception e)
        {
            LOGGER.warning("Unable to load $", resource);
            return null;
        }
    }

    @Override
    public int size()
    {
        return files.size();
    }

    @Override
    public String toString()
    {
        return AsciiArt.bulleted(files);
    }
}
