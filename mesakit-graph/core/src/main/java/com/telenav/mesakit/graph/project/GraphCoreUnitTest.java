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

import com.telenav.kivakit.configuration.ConfigurationSet;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.resource.CopyMode;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
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
import com.telenav.mesakit.map.region.project.MapRegionUnitTest;
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
public abstract class GraphCoreUnitTest extends MapRegionUnitTest
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
        GraphCoreProject.get().initialize();
    }

    private final Location.DegreesConverter locationInDegreesConverter = new Location.DegreesConverter(LOGGER);

    private final Location.DegreesMinutesAndSecondsConverter locationInDegreesMinutesAndSecondsConverter =
            new Location.DegreesMinutesAndSecondsConverter(LOGGER);

    private final int nextUniDbEdgeIdentifier = 1;

    private int nextOsmEdgeIdentifier = 1;

    protected GraphCoreUnitTest()
    {
        final var store = ConfigurationSet.global();
        LOGGER.listenTo(store);
        store.addFolder(Folder.parse("configuration"));
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
        final var set = new EdgeSet(Estimate.estimate(edges));
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

    protected Edge osmDowntownSeattleTestEdge(final long identifier)
    {
        return osmDowntownSeattleTest().edgeForIdentifier(new EdgeIdentifier(identifier));
    }

    protected EdgeSet osmDowntownSeattleTestEdges(final long... identifiers)
    {
        final var edges = new EdgeSet(Estimate.estimate(identifiers.length));
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
    protected GraphCoreRandomValueFactory randomValueFactory()
    {
        return newRandomValueFactory(GraphCoreRandomValueFactory::new);
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

    protected HeavyWeightEdge testEdge(final long identifier)
    {
        final var edge = OsmDataSpecification.get().newHeavyWeightEdge(null, identifier);
        edge.populateWithTestValues();
        return edge;
    }

    protected Vertex vertex(final Graph graph, final double latitude, final double longitude)
    {
        return graph.vertexNearest(Location.degrees(latitude, longitude), Distance.meters(50));
    }

    private static Folder cacheFolder()
    {
        return GraphCoreProject.get().overpassFolder();
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
                final var destination = GraphCoreProject.get().graphFolder().folder("overpass");
                final var source = Folder.kivakitHome().folder("tdk-graph/core/data");
                source.copyTo(destination, CopyMode.OVERWRITE, Extension.OSM_PBF.fileMatcher(), ProgressReporter.NULL);
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
                        graph.save(new GraphArchive(graphFile, ZipArchive.Mode.WRITE, ProgressReporter.NULL));
                        return graph;
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
            return new GraphArchive(graphFile, READ, ProgressReporter.NULL).load(Listener.none());
        }
        return null;
    }
}
