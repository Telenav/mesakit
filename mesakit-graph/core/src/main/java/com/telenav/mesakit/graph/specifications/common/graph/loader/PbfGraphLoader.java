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

import com.telenav.kivakit.core.language.progress.ProgressReporter;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.all.AllRelationsFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.all.AllWaysFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.region.Region;

/**
 * A {@link PbfGraphLoader} loads data from PBF format into a {@link GraphStore} when {@link Graph#load(GraphLoader,
 * GraphConstraints)} is called with a configured {@link PbfGraphLoader} instance. The source of data to read is
 * configured by calling by calling {@link #dataSourceFactory(PbfDataSourceFactory, Metadata)}.
 * <p>
 * This base class contains configuration information available through {@link #configuration()} that is useful to
 * subclasses.  Which ways and relations are included in the graph can be determined with {@link
 * Configuration#wayFilter(WayFilter)} and a {@link Configuration#relationFilter(RelationFilter)}. These filters must be
 * explicitly provided by design and there is no default. This ensures that users of the framework think about which
 * ways and relations they should include.
 * <p>
 * A region to clean cut against can be specified with {@link Configuration#cleanCutTo(Region)}. Clean-cutting
 * introduces artificial nodes when ways exit the region in order to cut the way exactly at the border. This is in
 * contrast to "soft cutting" where ways that intersect the cutting region are not clipped at the border instead dangle
 * outside the cutting region.
 *
 * @author jonathanl (shibo)
 * @see PbfDataSource
 * @see SerialPbfReader
 * @see Region
 * @see PbfToGraphConverter
 */
@SuppressWarnings("rawtypes")
public abstract class PbfGraphLoader extends BaseGraphLoader
{
    public static Configuration newConfiguration(Metadata metadata)
    {
        return new Configuration();
    }

    /**
     * Configuration state for PBF graph converters
     */
    public static class Configuration
    {
        /**
         * The region to clean-cut ways against. "Clean cutting" creates an artificial node exactly at the border
         * whenever a way crosses the region boundary. This is contrasted with "soft cutting", where ways that
         * intersection the region dangle outside of the region border.
         */
        private Region cleanCutTo;

        /** A filter to restrict ways that are loaded */
        private WayFilter wayFilter = new AllWaysFilter();

        /** A filter to restrict the relations that are loaded */
        private RelationFilter relationFilter = new AllRelationsFilter();

        /** Filter to restrict tags that are retained */
        private PbfTagFilter tagFilter = PbfTagFilter.ALL;

        /** True to include region information (which can be expensive if RTree lookups are required) */
        private Boolean regionInformation = true;

        public Region cleanCutTo()
        {
            return cleanCutTo;
        }

        public Configuration cleanCutTo(Region cleanCutTo)
        {
            this.cleanCutTo = cleanCutTo;
            return this;
        }

        public Boolean regionInformation()
        {
            return regionInformation;
        }

        public Configuration regionInformation(Boolean regionInformation)
        {
            this.regionInformation = regionInformation;
            return this;
        }

        public RelationFilter relationFilter()
        {
            return relationFilter;
        }

        public Configuration relationFilter(RelationFilter relationFilter)
        {
            this.relationFilter = relationFilter;
            return this;
        }

        public void tagFilter(PbfTagFilter tagFilter)
        {
            this.tagFilter = tagFilter;
        }

        public PbfTagFilter tagFilter()
        {
            return tagFilter;
        }

        public WayFilter wayFilter()
        {
            return wayFilter;
        }

        public Configuration wayFilter(WayFilter wayFilter)
        {
            this.wayFilter = wayFilter;
            return this;
        }
    }

    /** The source of PBF data */
    private PbfDataSourceFactory dataSourceFactory;

    /** The phase of processing */
    private String phase;

    /** Information about the data being loaded from the data source */
    private Metadata metadata;

    /** Configuration of this graph loader */
    private Configuration configuration;

    /**
     * @param configuration The configuration for this loader
     */
    public void configure(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Specifies the data source to load from, such as a {@link SerialPbfReader}
     */
    public PbfGraphLoader dataSourceFactory(PbfDataSourceFactory dataSourceFactory, Metadata metadata)
    {
        this.dataSourceFactory = dataSourceFactory;
        this.metadata = metadata;
        return this;
    }

    /**
     * Sets the current phase of processing that's in progress. This is used when reporting progress via a {@link
     * ProgressReporter}
     */
    public PbfGraphLoader phase(String phase)
    {
        this.phase = phase;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resource()
    {
        return dataSourceFactory.resource();
    }

    /**
     * @return The configuration of this loader
     */
    protected Configuration configuration()
    {
        return configuration;
    }

    protected PbfDataSourceFactory dataSourceFactory()
    {
        return dataSourceFactory;
    }

    /**
     * @return Information about the data contained in {@link #resource()}
     */
    protected Metadata metadata()
    {
        return metadata;
    }

    protected String phase()
    {
        return phase;
    }
}
