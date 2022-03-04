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
import com.telenav.kivakit.core.progress.reporters.Progress;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.convert.GraphConverter;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.mesakit.graph.specifications.osm.graph.converter.OsmPbfToGraphConverter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import static com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory.Type.PARALLEL_READER;
import static com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory.Type.SERIAL_READER;

/**
 * @author jonathanl (shibo)
 */
public abstract class PbfToGraphConverter extends BaseRepeater implements GraphConverter
{
    public static Configuration defaultConfiguration()
    {
        return new Configuration();
    }

    public static Configuration newConfiguration(Metadata metadata)
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

        private File turnRestrictionsSideFile;

        private boolean verify;

        public Configuration addSideFilesTo(Graph graph)
        {
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

        public Configuration freeFlowSideFile(File freeFlowSideFile)
        {
            this.freeFlowSideFile = freeFlowSideFile;
            return this;
        }

        public PbfGraphLoader.Configuration loaderConfiguration()
        {
            return loaderConfiguration;
        }

        public Configuration loaderConfiguration(PbfGraphLoader.Configuration loaderConfiguration)
        {
            this.loaderConfiguration = loaderConfiguration;
            return this;
        }

        public Configuration parallel(boolean parallel)
        {
            this.parallel = parallel;
            return this;
        }

        public Configuration speedPatternFile(File speedPatternFile)
        {
            this.speedPatternFile = speedPatternFile;
            return this;
        }

        public Count threads()
        {
            return threads;
        }

        public void threads(Count threads)
        {
            this.threads = threads;
        }

        public Configuration turnRestrictionsSideFile(File turnRestrictionsSideFile)
        {
            this.turnRestrictionsSideFile = turnRestrictionsSideFile;
            return this;
        }

        public boolean verify()
        {
            return verify;
        }

        public Configuration verify(boolean verify)
        {
            this.verify = verify;
            return this;
        }
    }

    private Configuration configuration;

    private final Metadata metadata;

    protected PbfToGraphConverter(Metadata metadata)
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
    public void configure(Configuration configuration)
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
        var metadata = this.metadata != null ? this.metadata : Metadata.from(file);
        if (metadata != null)
        {
            // create a reader for the data,
            var input = listenTo(new PbfDataSourceFactory(file,
                    configuration().threads(), configuration().parallel ? PARALLEL_READER : SERIAL_READER));

            // convert the data to a graph,
            var graph = onConvert(input, metadata);
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
    protected abstract Graph onConvert(PbfDataSourceFactory input, Metadata metadata);

    /**
     * Allows the subclass to perform finalization of the graph
     *
     * @param graph The graph that has been converted
     */
    @MustBeInvokedByOverriders
    protected void onConverted(Graph graph)
    {
    }
}
