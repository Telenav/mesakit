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

package com.telenav.mesakit.graph.specifications.common.graph.loader;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.archive.GraphArchive;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.READ;
import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.WRITE;

/**
 * An OSM PBF resource that can be converted to a graph by calling {@link #graph()}.
 *
 * @author jonathanl (shibo)
 */
public class PbfFile extends BaseRepeater implements Named
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static boolean accepts(final File file)
    {
        final var fileName = file.fileName();
        if (fileName != null)
        {
            final var extension = fileName.compoundExtension();
            return extension != null && (extension.endsWith(Extension.OSM_PBF));
        }
        return false;
    }

    /** The file to convert */
    private final File file;

    /** The converter configuration */
    private final PbfToGraphConverter.Configuration configuration;

    /**
     * @param file The .osm.pbf input file
     * @param configuration The pbf converter configuration
     */
    public PbfFile(final File file, final PbfToGraphConverter.Configuration configuration)
    {
        ensure(accepts(file), "Resource '$' is not a $", file, Extension.GRAPH);

        this.configuration = configuration;
        this.file = file;
    }

    /**
     * @return The graph for this PBF resource
     */
    public Graph graph(final ProgressReporter reporter)
    {
        final var metadata = Metadata.from(file);
        if (metadata != null)
        {
            final var output = Folder.temporaryForProcess(Folder.Type.CLEAN_UP_ON_EXIT)
                    .temporaryFile(file.fileName().withoutExtension(Extension.OSM_PBF)
                            .withoutExtension(Extension.parse(".pbf.gz")), Extension.GRAPH);

            final var converter = (PbfToGraphConverter) metadata.dataSpecification().newGraphConverter(metadata);
            converter.configure(configuration);

            // Convert to a graph
            final var graph = converter.convert(file);
            if (graph != null)
            {
                // save to disk,
                reporter.phase("Writing");
                try (final var archive = new GraphArchive(output, WRITE, reporter))
                {
                    graph.save(archive);
                }
                LOGGER.information("Converted $ to $", file, output);

                // then reload it
                reporter.phase("Loading");
                @SuppressWarnings("resource") final var archive = new GraphArchive(output, READ, reporter);
                return archive.load(this);
            }
            else
            {
                problem("Unable to convert $ to graph", file);
                return null;
            }
        }

        problem("Unable to load metadata from '$'", file);
        return null;
    }

    public Time lastModified()
    {
        return file.lastModified();
    }

    @Override
    public String name()
    {
        return file.fileName().name();
    }

    @Override
    public String toString()
    {
        return name();
    }
}
