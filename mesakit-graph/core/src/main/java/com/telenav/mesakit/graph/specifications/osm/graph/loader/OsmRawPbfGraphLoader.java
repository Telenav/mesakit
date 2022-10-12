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

import com.telenav.kivakit.core.collections.map.ObjectMap;
import com.telenav.kivakit.core.string.Strings;
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
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.extractors.RevisionNumberExtractor;
import com.telenav.mesakit.map.data.formats.pbf.model.extractors.TimestampExtractor;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.RoadName;

import static com.telenav.mesakit.map.road.name.standardizer.RoadNameStandardizer.Mode.MESAKIT_STANDARDIZATION;

/**
 * Subclass of {@link RawPbfGraphLoader} which extracts OSM-specific attributes during raw graph loading.
 */
public final class OsmRawPbfGraphLoader extends RawPbfGraphLoader
{
    private final LaneCountExtractor laneCountExtractor;

    private final LastModifierExtractor lastModifierExtractor;

    private final RevisionNumberExtractor revisionExtractor;

    private final TimestampExtractor timestampExtractor;

    private final ObjectMap<MapLocale, OfficialRoadNameExtractor> officialRoadNameExtractorForLocale = new ObjectMap<>()
    {
        @Override
        protected OfficialRoadNameExtractor onCreateValue(MapLocale locale)
        {
            return new OfficialRoadNameExtractor(locale, MESAKIT_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    private final ObjectMap<MapLocale, ExitRoadNameExtractor> exitRoadNameExtractorForLocale = new ObjectMap<>()
    {
        @Override
        protected ExitRoadNameExtractor onCreateValue(MapLocale locale)
        {
            return new ExitRoadNameExtractor(locale, MESAKIT_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    private final ObjectMap<MapLocale, AlternateRoadNameExtractor> alternateRoadNameExtractorForLocale = new ObjectMap<>()
    {
        @Override
        protected AlternateRoadNameExtractor onCreateValue(MapLocale locale)
        {
            return new AlternateRoadNameExtractor(locale, MESAKIT_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    private final ObjectMap<MapLocale, RouteRoadNameExtractor> routeRoadNameExtractorForLocale = new ObjectMap<>()
    {
        @Override
        protected RouteRoadNameExtractor onCreateValue(MapLocale locale)
        {
            return new RouteRoadNameExtractor(locale, MESAKIT_STANDARDIZATION, OsmRawPbfGraphLoader.this);
        }
    };

    /** Data analysis */
    private final PbfDataAnalysis analysis;

    /**
     * @param dataSourceFactory OSM data source to read from
     * @param analysis Information about the data in the source
     */
    public OsmRawPbfGraphLoader(PbfDataSourceFactory dataSourceFactory, PbfDataAnalysis analysis,
                                PbfTagFilter filter)
    {
        super(dataSourceFactory, analysis.metadata(), filter);

        this.analysis = analysis;

        // Create extractors
        revisionExtractor = new RevisionNumberExtractor(this);
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
    protected ProcessingDirective onExtractEdge(ExtractedEdges extracted)
    {
        var edge = (OsmHeavyWeightEdge) extracted.edge();
        var way = extracted.way();

        // Extract lane count (for whole road in both directions if two way)
        var laneCount = laneCountExtractor.extract(way);
        edge.laneCount(laneCount);

        // Extract road names
        var locale = edge.country() == null ? MapLocale.ENGLISH_UNITED_STATES.get() : edge.country().locale();
        edge.roadNames(RoadName.Type.ALTERNATE, alternateRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.OFFICIAL, officialRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.EXIT, exitRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.ROUTE, routeRoadNameExtractorForLocale.getOrCreate(locale).extract(way));

        // Extract OSM metadata
        edge.pbfRevisionNumber(revisionExtractor.extract(way));
        edge.pbfChangeSetIdentifier(new PbfChangeSetIdentifier(way.changeSetIdentifier()));
        edge.lastModificationTime(timestampExtractor.extract(way));
        var user = lastModifierExtractor.extract(way);
        if (user != null)
        {
            if (!Strings.isNullOrEmpty(user.getName()))
            {
                edge.pbfUserName(new PbfUserName(user.getName()));
            }
            edge.pbfUserIdentifier(new PbfUserIdentifier(user.getId()));
        }

        // Continue processing the edge
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingNode(GraphStore store, PbfNode node)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingRelation(GraphStore store, PbfRelation relation)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingWay(GraphStore store, PbfWay way)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected boolean shouldStoreNodeLocation(PbfNode node)
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
