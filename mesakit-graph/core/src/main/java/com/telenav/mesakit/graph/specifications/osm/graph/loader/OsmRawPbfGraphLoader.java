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

package com.telenav.mesakit.graph.specifications.osm.graph.loader;

import com.telenav.kivakit.data.formats.pbf.model.change.*;
import com.telenav.kivakit.data.formats.pbf.model.extractors.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.kernel.language.collections.map.BoundedMap;
import com.telenav.kivakit.kernel.language.string.Strings;
import com.telenav.mesakit.graph.specifications.common.graph.loader.RawPbfGraphLoader;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.AlternateRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.ExitRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.LaneCountExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.OfficialRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.RouteRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataAnalysis;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.OsmHeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.extractors.LastModifierExtractor;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.RoadName;

import static com.telenav.kivakit.map.road.name.standardizer.RoadNameStandardizer.Mode.TDK_STANDARDIZATION;

/**
 * Subclass of {@link RawPbfGraphLoader} which extracts OSM-specific attributes during raw graph loading.
 */
public final class OsmRawPbfGraphLoader extends RawPbfGraphLoader
{
    private final LaneCountExtractor laneCountExtractor;

    private final LastModifierExtractor lastModifierExtractor;

    private final RevisionExtractor revisionExtractor;

    private final TimestampExtractor timestampExtractor;

    private final BoundedMap<MapLocale, OfficialRoadNameExtractor> officialRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected OfficialRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new OfficialRoadNameExtractor(locale, TDK_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    private final BoundedMap<MapLocale, ExitRoadNameExtractor> exitRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected ExitRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new ExitRoadNameExtractor(locale, TDK_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    private final BoundedMap<MapLocale, AlternateRoadNameExtractor> alternateRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected AlternateRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new AlternateRoadNameExtractor(locale, TDK_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    private final BoundedMap<MapLocale, RouteRoadNameExtractor> routeRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected RouteRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new RouteRoadNameExtractor(locale, TDK_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    /** Data analysis */
    private final PbfDataAnalysis analysis;

    /**
     * @param dataSourceFactory OSM data source to read from
     * @param analysis Information about the data in the source
     */
    public OsmRawPbfGraphLoader(final PbfDataSourceFactory dataSourceFactory, final PbfDataAnalysis analysis,
                                final PbfTagFilter filter)
    {
        super(dataSourceFactory, analysis.metadata(), filter);

        this.analysis = analysis;

        // Create extractors
        revisionExtractor = new RevisionExtractor(this);
        timestampExtractor = new TimestampExtractor(this);
        laneCountExtractor = new LaneCountExtractor(this);
        lastModifierExtractor = new LastModifierExtractor(this);
    }

    @Override
    protected void onEndNodes()
    {
        analysis.freeWayNodes();
    }

    @Override
    protected ProcessingDirective onExtractEdge(final ExtractedEdges extracted)
    {
        final var edge = (OsmHeavyWeightEdge) extracted.edge();
        final var way = extracted.way();

        // Extract lane count (for whole road in both directions if two way)
        final var laneCount = laneCountExtractor.extract(way);
        edge.laneCount(laneCount);

        // Extract road names
        final var locale = edge.country() == null ? MapLocale.ENGLISH_UNITED_STATES.get() : edge.country().locale();
        edge.roadNames(RoadName.Type.ALTERNATE, alternateRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.OFFICIAL, officialRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.EXIT, exitRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.ROUTE, routeRoadNameExtractorForLocale.getOrCreate(locale).extract(way));

        // Extract OSM metadata
        edge.pbfRevisionNumber(new PbfRevisionNumber(revisionExtractor.extract(way)));
        edge.pbfChangeSetIdentifier(new PbfChangeSetIdentifier(way.changeSetIdentifier()));
        edge.lastModificationTime(timestampExtractor.extract(way));
        final var user = lastModifierExtractor.extract(way);
        if (user != null)
        {
            if (!Strings.isEmpty(user.getName()))
            {
                edge.pbfUserName(new PbfUserName(user.getName()));
            }
            edge.pbfUserIdentifier(new PbfUserIdentifier(user.getId()));
        }

        // Continue processing the edge
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingNode(final GraphStore store, final PbfNode node)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingRelation(final GraphStore store, final PbfRelation relation)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingWay(final GraphStore store, final PbfWay way)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected boolean shouldStoreNodeLocation(final PbfNode node)
    {
        // If the file has way node locations
        if (analysis.hasWayNodeLocations())
        {
            // we don't need to store them
            return false;
        }

        // otherwise, we need to keep the locations of way nodes only
        return analysis.wayNodes().contains(node.identifierAsLong());
    }
}
