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

package com.telenav.mesakit.graph.specifications.osm.graph.converter;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfGraphLoader;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfToGraphConverter;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataAnalysis;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.mesakit.graph.specifications.osm.graph.OsmGraph;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.OsmPbfGraphLoader;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.OsmRawPbfGraphLoader;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.region.Region;

import static com.telenav.mesakit.graph.Metadata.CountType.REQUIRE_EXACT;
import static com.telenav.mesakit.graph.specifications.library.pbf.PbfDataAnalysis.AnalysisType;
import static com.telenav.mesakit.graph.specifications.library.pbf.PbfDataAnalysis.AnalysisType.FULL_NODE_INFORMATION;

/**
 * OSM graph conversion takes three passes through the PBF data:
 * <ul>
 * <li>Pass 1: Read {@link PbfDataAnalysis} from the PBF file. This creates tag compression
 * codecs tuned to the data and determines which nodes represent intersections.</li>
 * <li>Pass 2: {@link PbfGraphLoader} uses {@link OsmRawPbfGraphLoader} to load a raw graph and then
 * sections it into edges using the intersection information acquired during the first pass</li>
 * <li>Pass 3: {@link PbfGraphLoader} adds tag information to vertexes created by edge sectioning
 * during the second pass</li>
 * </ul>
 * Once the data is loaded, these steps take place in the OsmPbfToGraphConverter class:
 * <ul>
 * <li>1. Mark double-digitized edges</li>
 * <li>2. Mark ramps and connectors</li>
 * <li>3. Mark junction edges</li>
 * <li>4. Load any sidefiles for trace counts, free flow or turn restrictions</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see PbfToGraphConverter
 */
public class OsmPbfToGraphConverter extends PbfToGraphConverter
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public static class Configuration extends PbfToGraphConverter.Configuration
    {
        private boolean markDoubleDigitizedEdges = true;

        private boolean markRampsAndConnectors = true;

        private boolean markJunctionEdges = true;

        private AnalysisType analysisType = AnalysisType.DEFAULT;

        public PbfToGraphConverter.Configuration analysisType(AnalysisType analysisType)
        {
            this.analysisType = analysisType;
            if (analysisType == FULL_NODE_INFORMATION)
            {
                LOGGER.information("Storing full node information for graph enhancement");
            }
            return this;
        }

        public void markDoubleDigitizedEdges(boolean markDoubleDigitizedEdges)
        {
            this.markDoubleDigitizedEdges = markDoubleDigitizedEdges;
        }

        public void markJunctionEdges(boolean markJunctionEdges)
        {
            this.markJunctionEdges = markJunctionEdges;
        }

        public void markRampsAndConnectors(boolean markRampsAndConnectors)
        {
            this.markRampsAndConnectors = markRampsAndConnectors;
        }
    }

    public OsmPbfToGraphConverter(Metadata metadata)
    {
        super(metadata);
    }

    @Override
    public Configuration configuration()
    {
        return (Configuration) super.configuration();
    }

    /**
     * Converts the input data to a graph
     *
     * @return The converted graph
     */
    @Override
    protected Graph onConvert(PbfDataSourceFactory input, Metadata metadata)
    {
        // Load borders in background while analyzing the input file.
        if (metadata.isOsm())
        {
            Region.loadBordersInBackground();
        }

        // Analyze the input file,
        var analysis = analyze(input.newInstance(metadata),
                metadata.withDataPrecision(Precision.DM6));

        // and if there is at least some valid data to load
        if (analysis.isValid())
        {
            // then load the graph by processing the input again
            return load(input, analysis);
        }

        warning("PBF input $ did not have at least one valid way", input);
        return null;
    }

    /**
     * Do post processing to mark double-digitized edges, ramps, connectors and junction edges.
     */
    @Override
    protected void onConverted(Graph uncast)
    {
        super.onConverted(uncast);

        var graph = (OsmGraph) uncast;

        if (configuration().markDoubleDigitizedEdges)
        {
            graph.markDoubleDigitizedEdges();
        }
        if (configuration().markRampsAndConnectors)
        {
            graph.markRampsAndConnectors();
        }
        if (configuration().markJunctionEdges)
        {
            graph.markJunctionEdges();
        }
    }

    private PbfDataAnalysis analyze(PbfDataSource input, Metadata metadata)
    {
        var loaderConfiguration = configuration().loaderConfiguration();
        var analysis = listenTo(new PbfDataAnalysis(metadata, configuration().analysisType, loaderConfiguration.wayFilter()));
        analysis.analyze(input);

        if (analysis.metadata().relationCount(REQUIRE_EXACT).isZero())
        {
            warning("OSM file is missing relation information");
        }

        DEBUG.trace(metadata.asString());

        return analysis;
    }

    private Graph load(PbfDataSourceFactory dataSource, PbfDataAnalysis analysis)
    {
        // Create graph to load with data
        var metadata = analysis.metadata();

        var graph = listenTo(metadata.newGraph());

        // Load graph from PBF
        var loader = listenTo(new OsmPbfGraphLoader());
        loader.configure(configuration().loaderConfiguration());
        loader.analysis(analysis, configuration().loaderConfiguration().tagFilter());
        loader.dataSourceFactory(dataSource, metadata);

        var loadedMetadata = graph.load(loader, GraphConstraints.ALL);
        if (loadedMetadata != null && loadedMetadata.isValid())
        {
            return graph;
        }
        return null;
    }
}
