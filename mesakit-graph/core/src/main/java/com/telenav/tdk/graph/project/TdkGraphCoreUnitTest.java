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

package com.telenav.kivakit.graph.project;

import com.telenav.kivakit.configuration.ConfigurationStore;
import com.telenav.kivakit.filesystem.*;
import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.operation.progress.ProgressReporter;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.data.formats.pbf.processing.filters.osm.*;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.collections.EdgeSet;
import com.telenav.kivakit.graph.identifiers.*;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.metadata.DataSpecification;
import com.telenav.kivakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.kivakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.kivakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.kivakit.graph.specifications.library.pbf.PbfFileMetadataAnnotator;
import com.telenav.kivakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.kivakit.graph.specifications.osm.graph.OsmGraph;
import com.telenav.kivakit.graph.specifications.unidb.UniDbDataSpecification;
import com.telenav.kivakit.graph.specifications.unidb.graph.UniDbGraph;
import com.telenav.kivakit.map.geography.*;
import com.telenav.kivakit.map.geography.rectangle.Rectangle;
import com.telenav.kivakit.map.measurements.Distance;
import com.telenav.kivakit.map.overpass.OverpassDataDownloader;
import com.telenav.kivakit.map.region.Country;
import com.telenav.kivakit.map.region.project.KivaKitMapRegionUnitTest;
import org.junit.BeforeClass;

import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.READ;
import static com.telenav.kivakit.data.formats.library.DataFormat.PBF;
import static com.telenav.kivakit.graph.metadata.DataSupplier.*;
import static com.telenav.kivakit.graph.specifications.library.pbf.PbfFileMetadataAnnotator.Mode.STRIP_UNREFERENCED_NODES;

/**
 * Base class for graph unit tests.
 *
 * @author jonathanl (shibo)
 */
public abstract class KivaKitGraphCoreUnitTest extends KivaKitMapRegionUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static int nextOsmRelationIdentifier = 1;

    private static final Lazy<Graph> osmGreenLakeSeattleLarge = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Green_Lake_Seattle_Large",
                    Country.UNITED_STATES.WASHINGTON.SEATTLE.GREEN_LAKE.bounds().expanded(Distance.ONE_MILE)));

    private static final Lazy<Graph> osmGreenLakeSeattle = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Green_Lake_Seattle", Country.UNITED_STATES.WASHINGTON.SEATTLE.GREEN_LAKE.bounds()));

    private static final Lazy<Graph> osmDowntownSeattle = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Downtown_Seattle", Country.UNITED_STATES.WASHINGTON.SEATTLE.DOWNTOWN.bounds()));

    private static final Lazy<Graph> osmDowntownSeattleTest = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Downtown_Seattle_Test", Rectangle.parse("47.587309,-122.346791:47.616221,-122.317879")));

    private static final Lazy<Graph> osmBellevueWashington = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Bellevue_Washington", Location.degrees(47.61302, -122.188).within(Distance.miles(2))));

    private static final Lazy<Graph> osmBuffalo = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Buffalo_New_York", Country.UNITED_STATES.NEW_YORK.BUFFALO.bounds()));

    private static final Lazy<Graph> osmHuronCharter = new Lazy<>(
            () -> graph(OsmDataSpecification.get(), "Huron_Charter", Rectangle.fromLocations(Location.degrees(42.179459, -83.423221),
                    Location.degrees(42.094242, -83.303885))));

    private static final Lazy<Graph> uniDbDowntownSanFrancisco = new Lazy<>(
            () -> graph(UniDbDataSpecification.get(), "Downtown_San_Francisco", Rectangle.parse("37.77205,-122.42746:37.79907,-122.39553")));

    public static Graph osmBellevueWashington()
    {
        return osmBellevueWashington.get();
    }

    public static Graph osmBuffalo()
    {
        return osmBuffalo.get();
    }

    public static Graph osmDowntownSeattle()
    {
        return osmDowntownSeattle.get();
    }

    public static Graph osmDowntownSeattleTest()
    {
        return osmDowntownSeattleTest.get();
    }

    public static Graph osmGreenLakeSeattle()
    {
        return osmGreenLakeSeattle.get();
    }

    public static Graph osmGreenLakeSeattleLarge()
    {
        return osmGreenLakeSeattleLarge.get();
    }

    public static Graph osmHuronCharter()
    {
        return osmHuronCharter.get();
    }

    @BeforeClass
    public static void testSetup()
    {
        KivaKitGraphCore.get().install();
    }

    public static Graph uniDbDowntownSanFrancisco()
    {
        return uniDbDowntownSanFrancisco.get();
    }

    private final Location.DegreesConverter locationInDegreesConverter = new Location.DegreesConverter(LOGGER);

    private final Location.DegreesMinutesAndSecondsConverter locationInDegreesMinutesAndSecondsConverter =
            new Location.DegreesMinutesAndSecondsConverter(LOGGER);

    private int nextUniDbEdgeIdentifier = 1;

    private int nextOsmEdgeIdentifier = 1;

    protected KivaKitGraphCoreUnitTest()
    {
        final var store = ConfigurationStore.global();
        LOGGER.listenTo(store);
        store.loadAll(new Folder("configuration"));
    }

    protected Edge edge(final Graph graph, final double fromLatitude, final double fromLongitude,
                        final double toLatitude, final double toLongitude)
    {
        final var from = vertex(graph, fromLatitude, fromLongitude);
        final var to = vertex(graph, toLatitude, toLongitude);
        if (from != null && to != null)
        {
            return from.edgeTo(to);
        }
        return null;
    }

    protected Edge edgeNear(final Graph graph, final double latitude, final double longitude)
    {
        return graph.edgeNearest(Location.degrees(latitude, longitude), Distance.meters(20));
    }

    protected EdgeSet edges(final Edge... edges)
    {
        final var set = new EdgeSet(Estimate.of(edges));
        set.addAll(edges);
        return set;
    }

    protected Location location(final String location)
    {
        return locationInDegreesConverter.convert(location);
    }

    protected Location locationInDegreesMinutesAndSeconds(final String location)
    {
        return locationInDegreesMinutesAndSecondsConverter.convert(location);
    }

    protected HeavyWeightEdge nextOsmEdge(final Graph graph)
    {
        final var edge = osmEdge(graph, nextOsmEdgeIdentifier, nextOsmEdgeIdentifier);
        nextOsmEdgeIdentifier++;
        return edge;
    }

    protected HeavyWeightRelation nextOsmRelation()
    {
        final var relation = osmRelation(nextOsmRelationIdentifier, nextOsmRelationIdentifier);
        nextOsmRelationIdentifier++;
        return relation;
    }

    protected HeavyWeightEdge nextUniDbEdge()
    {
        final var edge = uniDbEdge(nextUniDbEdgeIdentifier, nextUniDbEdgeIdentifier);
        nextUniDbEdgeIdentifier++;
        return edge;
    }

    protected Edge osmDowntownSeattleTestEdge(final long identifier)
    {
        return osmDowntownSeattleTest().edgeForIdentifier(new EdgeIdentifier(identifier));
    }

    protected EdgeSet osmDowntownSeattleTestEdges(final long... identifiers)
    {
        final var edges = new EdgeSet(Estimate.of(identifiers.length));
        for (final var identifier : identifiers)
        {
            edges.add(osmDowntownSeattleTestEdge(identifier));
        }
        return edges;
    }

    protected Route osmDowntownSeattleTestRoute(final long... identifiers)
    {
        final var builder = new RouteBuilder();
        for (final var identifier : identifiers)
        {
            builder.append(osmDowntownSeattleTestEdge(identifier));
        }
        return builder.route();
    }

    protected HeavyWeightEdge osmEdge(final Graph graph, final int index, final int identifier)
    {
        final var edge = graph.newHeavyWeightEdge(new EdgeIdentifier(identifier));
        edge.graph(graph);
        edge.populateWithTestValues();
        edge.index(index);
        return edge;
    }

    protected OsmGraph osmGraph()
    {
        return (OsmGraph) OsmDataSpecification.get().newGraph(Metadata.osm(OSM, PBF));
    }

    protected Edge osmGreenLakeSeattleEdge(final long identifier)
    {
        return osmGreenLakeSeattle().edgeForIdentifier(new EdgeIdentifier(identifier));
    }

    protected HeavyWeightRelation osmRelation(final int identifier, final int index)
    {
        final var relation = OsmDataSpecification.get().newHeavyWeightRelation(null, identifier);
        relation.index(index);
        return relation;
    }

    protected HeavyWeightVertex osmVertex(final Graph graph, final int index, final int identifier,
                                          final Location location)
    {
        final var vertex = graph.newHeavyWeightVertex(new VertexIdentifier(identifier));
        vertex.graph(graph);
        vertex.index(index);
        vertex.identifier(identifier);
        vertex.location(location);
        return vertex;
    }

    @Override
    protected KivaKitGraphCoreRandomValueFactory randomValueFactory()
    {
        return newRandomValueFactory(KivaKitGraphCoreRandomValueFactory::new);
    }

    protected Route route(final Edge... edges)
    {
        final var builder = new RouteBuilder();
        for (final var edge : edges)
        {
            builder.append(edge);
        }
        return builder.route();
    }

    @Override
    protected void serializationTest(final Object object)
    {
        serializationTest(serializer(), object);
    }

    protected HeavyWeightEdge testEdge(final long identifier)
    {
        final var edge = OsmDataSpecification.get().newHeavyWeightEdge(null, identifier);
        edge.populateWithTestValues();
        return edge;
    }

    protected HeavyWeightEdge uniDbEdge(final int index, final int identifier)
    {
        final var edge = UniDbDataSpecification.get().newHeavyWeightEdge(uniDbGraph(), identifier);
        edge.index(index);
        return edge;
    }

    protected UniDbGraph uniDbGraph()
    {
        return (UniDbGraph) UniDbDataSpecification.get().newGraph(Metadata.unidb(HERE, PBF));
    }

    protected Vertex vertex(final Graph graph, final double latitude, final double longitude)
    {
        return graph.vertexNearest(Location.degrees(latitude, longitude), Distance.meters(50));
    }

    private static Folder cacheFolder()
    {
        return KivaKitGraphCore.get().overpassFolder();
    }

    private static void downloadFromOverpass(final String dataDescriptor, final Rectangle bounds)
    {
        LOGGER.information("Downloading $_$", dataDescriptor, bounds.toFileString());
        final var overpass = LOGGER.listenTo(new OverpassDataDownloader(cacheFolder()));
        final var pbf = overpass.pbf(dataDescriptor, bounds);

        // then make sure it has metadata
        var metadata = Metadata.from(pbf);
        if (metadata == null)
        {
            final var annotator = LOGGER.listenTo(new PbfFileMetadataAnnotator(
                    pbf, STRIP_UNREFERENCED_NODES, new OsmNavigableWayFilter(), new OsmRelationsFilter()));
            metadata = annotator.read()
                    .withDataPrecision(Precision.DM6)
                    .withMetadata(Metadata.parseDescriptor(dataDescriptor));
            annotator.write(metadata);
        }
    }

    private static File file(final String dataDescriptor, final Rectangle bounds)
    {
        return cacheFolder().file(dataDescriptor + "-" + bounds.toFileString());
    }

    @SuppressWarnings("resource")
    private static Graph graph(final DataSpecification specification, final String name, final Rectangle bounds)
    {
        // If we can't find the graph file
        final var dataDescriptor = (specification.isOsm() ? "OSM-OSM-PBF-" : "HERE-UniDb-PBF-") + name;
        final var graphFile = file(dataDescriptor, bounds).withExtension(Extension.GRAPH);
        if (!graphFile.exists())
        {
            // and the PBF file doesn't exist
            final var pbfFile = file(dataDescriptor, bounds).withExtension(Extension.OSM_PBF);
            if (!pbfFile.exists())
            {
                // then try to copy it from the test data folder
                final var destination = KivaKitGraphCore.get().graphFolder().folder("overpass");
                final var source = Folder.tdkHome().folder("tdk-graph/core/data");
                source.copyTo(destination, Extension.OSM_PBF.fileMatcher(), ProgressReporter.NULL);
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
                final Metadata metadata = Metadata.from(pbfFile);
                if (metadata != null)
                {
                    // and convert the PBF file to a graph file using the right kind of converter for the metadata
                    final var converter = LOGGER.listenTo(metadata.dataSpecification().newGraphConverter(metadata));
                    final var graph = converter.convert(pbfFile);
                    if (graph != null)
                    {
                        // and if we succeeded, then save the graph file and return the graph
                        graph.save(new GraphArchive(graphFile, ProgressReporter.NULL, ZipArchive.Mode.WRITE));
                        return graph;
                    }

                    LOGGER.problem("Unable to extract graph from $", pbfFile);
                }
                else
                {
                    LOGGER.problem("No metdata found in $", pbfFile);
                }
            }
            else
            {
                LOGGER.problem("Unable to install PBF file $", pbfFile);
            }
        }
        else
        {
            return new GraphArchive(graphFile, ProgressReporter.NULL, READ).load(Listener.NULL);
        }
        return null;
    }
}
