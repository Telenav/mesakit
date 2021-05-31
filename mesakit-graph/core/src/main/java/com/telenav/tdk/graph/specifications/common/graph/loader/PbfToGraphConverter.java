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

package com.telenav.kivakit.graph.specifications.common.graph.loader;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.kernel.operation.progress.reporters.Progress;
import com.telenav.kivakit.kernel.scalars.counts.Count;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.io.convert.GraphConverter;
import com.telenav.kivakit.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.kivakit.graph.specifications.osm.graph.converter.OsmPbfToGraphConverter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import static com.telenav.kivakit.graph.specifications.library.pbf.PbfDataSourceFactory.Type.*;

/**
 * @author jonathanl (shibo)
 */
public abstract class PbfToGraphConverter extends BaseRepeater<Message> implements GraphConverter
{
    public static Configuration defaultConfiguration()
    {
        return new Configuration();
    }

    public static Configuration newConfiguration(final Metadata metadata)
    {
        switch (metadata.dataSpecification().type())
        {
            case OSM:
                return new OsmPbfToGraphConverter.Configuration();

            case UniDb:
            default:
                return new Configuration();
        }
    }

    /**
     * Configuration state for PBF graph converters
     */
    public static class Configuration
    {
        private File freeFlowSideFile;

        private PbfGraphLoader.Configuration loaderConfiguration = new PbfGraphLoader.Configuration();

        private boolean parallel;

        private Count threads = Count._4;

        private File speedPatternFile;

        private File traceCountsSideFile;

        private File turnRestrictionsSideFile;

        private boolean verify;

        public Configuration addSideFilesTo(final Graph graph)
        {
            if (traceCountsSideFile != null)
            {
                graph.loadTraceCounts(traceCountsSideFile);
            }
            if (freeFlowSideFile != null)
            {
                graph.loadFreeFlow(freeFlowSideFile);
            }
            if (turnRestrictionsSideFile != null)
            {
                graph.loadTurnRestrictions(turnRestrictionsSideFile);
            }
            if (speedPatternFile != null)
            {
                graph.loadSpeedPattern(freeFlowSideFile);
            }
            return this;
        }

        public Configuration freeFlowSideFile(final File freeFlowSideFile)
        {
            this.freeFlowSideFile = freeFlowSideFile;
            return this;
        }

        public PbfGraphLoader.Configuration loaderConfiguration()
        {
            return loaderConfiguration;
        }

        public Configuration loaderConfiguration(final PbfGraphLoader.Configuration loaderConfiguration)
        {
            this.loaderConfiguration = loaderConfiguration;
            return this;
        }

        public Configuration parallel(final boolean parallel)
        {
            this.parallel = parallel;
            return this;
        }

        public Configuration speedPatternFile(final File speedPatternFile)
        {
            this.speedPatternFile = speedPatternFile;
            return this;
        }

        public Count threads()
        {
            return threads;
        }

        public void threads(final Count threads)
        {
            this.threads = threads;
        }

        public Configuration traceCountsSideFile(final File traceCountsSideFile)
        {
            this.traceCountsSideFile = traceCountsSideFile;
            return this;
        }

        public Configuration turnRestrictionsSideFile(final File turnRestrictionsSideFile)
        {
            this.turnRestrictionsSideFile = turnRestrictionsSideFile;
            return this;
        }

        public boolean verify()
        {
            return verify;
        }

        public Configuration verify(final boolean verify)
        {
            this.verify = verify;
            return this;
        }
    }

    private Configuration configuration;

    private final Metadata metadata;

    protected PbfToGraphConverter(final Metadata metadata)
    {
        this.metadata = metadata;
        configuration = newConfiguration(metadata);
    }

    /**
     * @return The configuration of this converter
     */
    public Configuration configuration()
    {
        return configuration;
    }

    /**
     * @param configuration The configuration for this converter
     */
    @MustBeInvokedByOverriders
    public void configure(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Graph convert(File file)
    {
        // Ensure that the file is local
        file = file.materialized(Progress.create(this));

        // Extract metadata from the file,
        final var metadata = this.metadata != null ? this.metadata : Metadata.from(file);
        if (metadata != null)
        {
            // create a reader for the data,
            final var input = listenTo(new PbfDataSourceFactory(file,
                    configuration().threads(), configuration().parallel ? PARALLEL_READER : SERIAL_READER));

            // convert the data to a graph,
            final var graph = onConvert(input, metadata);
            if (graph != null)
            {
                // then let the subclass add anything else it wants
                onConverted(graph);

                // and finally, add side-files to the graph
                configuration.addSideFilesTo(graph);
            }
            return graph;
        }
        else
        {
            problem("Unable to extract metadata from $", file);
        }
        return null;
    }

    /**
     * The subclass overrides this method to convert the input data source, of the type described by the given metadata,
     * to a {@link Graph}.
     *
     * @return The converted graph
     */
    @MustBeInvokedByOverriders
    protected abstract Graph onConvert(final PbfDataSourceFactory input, final Metadata metadata);

    /**
     * Allows the subclass to perform finalization of the graph
     *
     * @param graph The graph that has been converted
     */
    @MustBeInvokedByOverriders
    protected void onConverted(final Graph graph)
    {
    }
}
