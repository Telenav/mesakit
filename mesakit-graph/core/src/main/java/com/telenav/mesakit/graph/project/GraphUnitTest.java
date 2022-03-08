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

package com.telenav.mesakit.graph.project;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.resource.CopyMode;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.settings.Settings;
import com.telenav.kivakit.settings.stores.FolderSettingsStore;
import com.telenav.mesakit.core.MesaKit;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphProject;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.RouteBuilder;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfFileMetadataAnnotator;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.graph.specifications.osm.graph.OsmGraph;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmNavigableWayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmRelationsFilter;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.overpass.OverpassDataDownloader;
import com.telenav.mesakit.map.region.project.RegionUnitTest;
import com.telenav.mesakit.map.region.regions.Country;
import org.junit.BeforeClass;

import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.READ;
import static com.telenav.mesakit.graph.metadata.DataSupplier.OSM;
import static com.telenav.mesakit.graph.specifications.library.pbf.PbfFileMetadataAnnotator.Mode.STRIP_UNREFERENCED_NODES;
import static com.telenav.mesakit.map.data.formats.library.DataFormat.PBF;

/**
 * Base class for graph unit tests.
 *
 * @author jonathanl (shibo)
 */
public abstract class GraphUnitTest extends RegionUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static int nextOsmRelationIdentifier = 1;

    private static final Lazy<Graph> osmGreenLakeSeattleLarge = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Green_Lake_Seattle_Large",
                    Country.UNITED_STATES.WASHINGTON.SEATTLE.GREEN_LAKE.bounds().expanded(Distance.ONE_MILE)));

    private static final Lazy<Graph> osmGreenLakeSeattle = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Green_Lake_Seattle", Country.UNITED_STATES.WASHINGTON.SEATTLE.GREEN_LAKE.bounds()));

    private static final Lazy<Graph> osmDowntownSeattle = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Downtown_Seattle", Country.UNITED_STATES.WASHINGTON.SEATTLE.DOWNTOWN.bounds()));

    private static final Lazy<Graph> osmDowntownSeattleTest = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Downtown_Seattle_Test", Rectangle.parse("47.587309,-122.346791:47.616221,-122.317879")));

    private static final Lazy<Graph> osmBellevueWashington = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Bellevue_Washington", Location.degrees(47.61302, -122.188).within(Distance.miles(2))));

    private static final Lazy<Graph> osmBuffalo = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Buffalo_New_York", Country.UNITED_STATES.NEW_YORK.BUFFALO.bounds()));

    private static final Lazy<Graph> osmHuronCharter = Lazy.of(
            () -> graph(OsmDataSpecification.get(), "Huron_Charter", Rectangle.fromLocations(Location.degrees(42.179459, -83.423221),
                    Location.degrees(42.094242, -83.303885))));

    public static synchronized Graph osmBellevueWashington()
    {
        return osmBellevueWashington.get();
    }

    public static synchronized Graph osmBuffalo()
    {
        return osmBuffalo.get();
    }

    public static synchronized Graph osmDowntownSeattle()
    {
        return osmDowntownSeattle.get();
    }

    public static synchronized Graph osmDowntownSeattleTest()
    {
        return osmDowntownSeattleTest.get();
    }

    public static synchronized Graph osmGreenLakeSeattle()
    {
        return osmGreenLakeSeattle.get();
    }

    public static synchronized Graph osmGreenLakeSeattleLarge()
    {
        return osmGreenLakeSeattleLarge.get();
    }

    public static synchronized Graph osmHuronCharter()
    {
        return osmHuronCharter.get();
    }

    @BeforeClass
    public static synchronized void testSetup()
    {
        GraphProject.get().initialize();
    }

    private final Location.DegreesConverter locationInDegreesConverter = new Location.DegreesConverter(LOGGER);

    private final Location.DegreesMinutesAndSecondsConverter locationInDegreesMinutesAndSecondsConverter =
            new Location.DegreesMinutesAndSecondsConverter(LOGGER);

    private int nextOsmEdgeIdentifier = 1;

    protected GraphUnitTest()
    {
        var store = Settings.of(this);
        LOGGER.listenTo(store);
        store.registerSettingsIn(FolderSettingsStore.of(this, Folder.parse(this, "configuration")));
    }

    protected Edge edge(Graph graph, double fromLatitude, double fromLongitude,
                        double toLatitude, double toLongitude)
    {
        var from = vertex(graph, fromLatitude, fromLongitude);
        var to = vertex(graph, toLatitude, toLongitude);
        if (from != null && to != null)
        {
            return from.edgeTo(to);
        }
        return null;
    }

    protected Edge edgeNear(Graph graph, double latitude, double longitude)
    {
        return graph.edgeNearest(Location.degrees(latitude, longitude), Distance.meters(20));
    }

    protected EdgeSet edges(Edge... edges)
    {
        var set = new EdgeSet(Estimate.estimate(edges));
        set.addAll(edges);
        return set;
    }

    protected Location location(String location)
    {
        return locationInDegreesConverter.convert(location);
    }

    protected Location locationInDegreesMinutesAndSeconds(String location)
    {
        return locationInDegreesMinutesAndSecondsConverter.convert(location);
    }

    protected HeavyWeightEdge nextOsmEdge(Graph graph)
    {
        var edge = osmEdge(graph, nextOsmEdgeIdentifier, nextOsmEdgeIdentifier);
        nextOsmEdgeIdentifier++;
        return edge;
    }

    protected HeavyWeightRelation nextOsmRelation()
    {
        var relation = osmRelation(nextOsmRelationIdentifier, nextOsmRelationIdentifier);
        nextOsmRelationIdentifier++;
        return relation;
    }

    protected Edge osmDowntownSeattleTestEdge(long identifier)
    {
        return osmDowntownSeattleTest().edgeForIdentifier(new EdgeIdentifier(identifier));
    }

    protected EdgeSet osmDowntownSeattleTestEdges(long... identifiers)
    {
        var edges = new EdgeSet(Estimate.estimate(identifiers.length));
        for (var identifier : identifiers)
        {
            edges.add(osmDowntownSeattleTestEdge(identifier));
        }
        return edges;
    }

    protected Route osmDowntownSeattleTestRoute(long... identifiers)
    {
        var builder = new RouteBuilder();
        for (var identifier : identifiers)
        {
            builder.append(osmDowntownSeattleTestEdge(identifier));
        }
        return builder.route();
    }

    protected HeavyWeightEdge osmEdge(Graph graph, int index, int identifier)
    {
        var edge = graph.newHeavyWeightEdge(new EdgeIdentifier(identifier));
        edge.graph(graph);
        edge.populateWithTestValues();
        edge.index(index);
        return edge;
    }

    protected OsmGraph osmGraph()
    {
        return (OsmGraph) OsmDataSpecification.get().newGraph(Metadata.osm(OSM, PBF));
    }

    protected Edge osmGreenLakeSeattleEdge(long identifier)
    {
        return osmGreenLakeSeattle().edgeForIdentifier(new EdgeIdentifier(identifier));
    }

    protected HeavyWeightRelation osmRelation(int identifier, int index)
    {
        var relation = OsmDataSpecification.get().newHeavyWeightRelation(null, identifier);
        relation.index(index);
        return relation;
    }

    protected HeavyWeightVertex osmVertex(Graph graph, int index, int identifier,
                                          Location location)
    {
        var vertex = graph.newHeavyWeightVertex(new VertexIdentifier(identifier));
        vertex.graph(graph);
        vertex.index(index);
        vertex.identifier(identifier);
        vertex.location(location);
        return vertex;
    }

    @Override
    protected GraphRandomValueFactory randomValueFactory()
    {
        return newRandomValueFactory(GraphRandomValueFactory::new);
    }

    protected Route route(Edge... edges)
    {
        var builder = new RouteBuilder();
        for (var edge : edges)
        {
            builder.append(edge);
        }
        return builder.route();
    }

    protected HeavyWeightEdge testEdge(long identifier)
    {
        var edge = OsmDataSpecification.get().newHeavyWeightEdge(null, identifier);
        edge.populateWithTestValues();
        return edge;
    }

    protected Vertex vertex(Graph graph, double latitude, double longitude)
    {
        return graph.vertexNearest(Location.degrees(latitude, longitude), Distance.meters(50));
    }

    private static Folder cacheFolder()
    {
        return GraphProject.get().overpassFolder();
    }

    private static void downloadFromOverpass(String dataDescriptor, Rectangle bounds)
    {
        LOGGER.information("Downloading $_$", dataDescriptor, bounds.toFileString());
        var overpass = LOGGER.listenTo(new OverpassDataDownloader(cacheFolder()));
        var pbf = overpass.pbf(dataDescriptor, bounds);

        // then make sure it has metadata
        var metadata = Metadata.from(pbf);
        if (metadata == null)
        {
            var annotator = LOGGER.listenTo(new PbfFileMetadataAnnotator(
                    pbf, STRIP_UNREFERENCED_NODES, new OsmNavigableWayFilter(), new OsmRelationsFilter()));
            metadata = annotator.read()
                    .withDataPrecision(Precision.DM6)
                    .withMetadata(Metadata.parseDescriptor(dataDescriptor));
            annotator.write(metadata);
        }
    }

    private static File file(String dataDescriptor, Rectangle bounds)
    {
        return cacheFolder().file(dataDescriptor + "-" + bounds.toFileString());
    }

    @SuppressWarnings("resource")
    private static Graph graph(DataSpecification specification,
                               String name,
                               Rectangle bounds)
    {
        // If we can't find the graph file
        var dataDescriptor = "OSM-OSM-PBF-" + name;
        var graphFile = file(dataDescriptor, bounds).withExtension(Extension.GRAPH);
        if (!graphFile.exists())
        {
            // and the PBF file doesn't exist
            var pbfFile = file(dataDescriptor, bounds).withExtension(Extension.OSM_PBF);
            if (!pbfFile.exists())
            {
                // then try to copy it from the test data folder
                var destination = LOGGER.listenTo(GraphProject.get().graphFolder().folder("overpass"));
                var source = LOGGER.listenTo(MesaKit.get().mesakitHome().folder("mesakit-graph/core/data"));
                source.copyTo(destination, CopyMode.OVERWRITE, Extension.OSM_PBF.fileMatcher(), ProgressReporter.none());
            }

            // and if we can't find it there and it's an OSM graph being requested,
            if (!pbfFile.exists() && specification.isOsm())
            {
                // then download the area from overpass.
                downloadFromOverpass(dataDescriptor, bounds);
            }

            // Now that the PBF file exists,
            if (pbfFile.exists())
            {
                // get its metadata
                Metadata metadata = Metadata.from(pbfFile);
                if (metadata != null)
                {
                    // and convert the PBF file to a graph file using the right kind of converter for the metadata
                    var converter = LOGGER.listenTo(metadata.dataSpecification().newGraphConverter(metadata));
                    var graph = converter.convert(pbfFile);
                    if (graph != null)
                    {
                        // and if we succeeded, then save the graph file and return the graph
                        graph.save(new GraphArchive(LOGGER, graphFile, ZipArchive.Mode.WRITE, ProgressReporter.none()));
                        return LOGGER.listenTo(graph);
                    }

                    LOGGER.problem("Unable to extract graph from $", pbfFile);
                }
                else
                {
                    LOGGER.problem("No metadata found in $", pbfFile);
                }
            }
            else
            {
                LOGGER.problem("Unable to install PBF file $", pbfFile);
            }
        }
        else
        {
            return new GraphArchive(LOGGER, graphFile, READ, ProgressReporter.none()).load(Listener.none());
        }
        return null;
    }
}
