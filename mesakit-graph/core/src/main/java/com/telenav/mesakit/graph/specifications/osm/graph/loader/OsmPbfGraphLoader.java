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

import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.vm.Properties;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.mesakit.graph.GraphProject;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.identifiers.collections.WayIdentifierList;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfGraphLoader;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfToGraphConverter;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataAnalysis;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.EdgeSectioner;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.WaySectioningGraphLoader;
import com.telenav.mesakit.graph.ui.debuggers.edge.sectioner.VisualEdgeSectionDebugger;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.project.Project.resolveProject;
import static com.telenav.kivakit.core.vm.Properties.isPropertyTrue;
import static com.telenav.mesakit.graph.Metadata.VALIDATE_EXCEPT_STATISTICS;

/**
 * Loads OSM data from PBF format into a {@link GraphStore}. During this process, ways are broken up into edges in a
 * process referred to as "way sectioning". Way sectioning is performed by a {@link WaySectioningGraphLoader}, which
 * takes OSM ways (in the form of a raw graph where each edge is a complete way) and breaks them down. This process is
 * fairly complex and significantly slows the process of loading OSM graphs versus UniDb graphs, which are already
 * sectioned into edges during processing by the data team.
 *
 * @author jonathanl (shibo)
 * @see GraphLoader
 * @see WaySectioningGraphLoader
 * @see PbfGraphLoader
 * @see PbfToGraphConverter
 */
public final class OsmPbfGraphLoader extends PbfGraphLoader
{
    /** Data from the first pass of analyzing the PBF input */
    private PbfDataAnalysis analysis;

    /** Tag filter for deciding which tags to include */
    private PbfTagFilter tagFilter;

    /**
     * @param analysis Resource analysis from the first pass through the PBF data. This data helps to make loading more
     * efficient and provides information about way intersections for the way sectioning process.
     */
    public OsmPbfGraphLoader analysis(PbfDataAnalysis analysis, PbfTagFilter tagFilter)
    {
        this.analysis = analysis;
        this.tagFilter = tagFilter;
        ensure(analysis.isValid());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCommit(GraphStore store)
    {
        // Store OSM node information
        store.vertexStore().allPbfNodeDiskStores(analysis.pbfNodeDiskStores());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "UnusedAssignment", "SpellCheckingInspection" })
    @Override
    public Metadata onLoad(GraphStore store, GraphConstraints constraints)
    {
        // Get the destination graph that we're loading with data from its graph store
        var destination = store.graph();
        var graphName = destination.metadata().name();

        // Load the raw, un-sectioned data into a graph,
        var loader = listenTo(new OsmRawPbfGraphLoader(dataSourceFactory(), analysis, tagFilter));
        loader.configure(configuration());
        var raw = destination.createCompatible();
        raw.addListener(this);
        var metadata = raw.load(loader, constraints);
        raw.name("Raw_" + graphName);

        // and if the load operation succeeded,
        if (metadata != null)
        {
            // check the metadata and report the result of this first step.
            metadata.assertValid(VALIDATE_EXCEPT_STATISTICS);

            // Next, estimate how many sectioned edges there will be
            information(AsciiArt.topLine(30, "Sectioning Ways"));
            var estimatedSectionedEdges = raw.forwardEdgeCount().times(6);
            information("Estimating there will be $ sectioned edges", estimatedSectionedEdges);

            // put the estimate into the graph so its graph store will allocate data structures accordingly,
            destination.metadata(metadata
                    .withEdgeCount(estimatedSectionedEdges)
                    .withName(graphName));

            if (isPropertyTrue("MESAKIT_DEBUG_SAVE_RAW_GRAPH"))
            {
                raw.save(new GraphArchive(this, resolveProject(GraphProject.class).userGraphFolder().file("raw.graph"), ZipArchive.Mode.WRITE, ProgressReporter.none()));
            }

            // and then section the raw graph by loading it into the destination graph
            var edgeSectioner = listenTo(new EdgeSectioner(
                    destination, analysis, loader.edgeNodes(), Distance.MAXIMUM));
            var waySectioner = new WaySectioningGraphLoader(raw, edgeSectioner);
            var ways = Properties.property("MESAKIT_DEBUG_WAY_SECTIONS");
            if (ways != null)
            {
                var wayIdentifiers = WayIdentifierList.parse(ways).asSet();
                if (wayIdentifiers != null)
                {
                    edgeSectioner.debugger(new VisualEdgeSectionDebugger(), wayIdentifiers);
                }
            }
            waySectioner.addListener(this);
            metadata = destination.load(waySectioner, constraints);
            information(AsciiArt.bottomLine(30, "Sectioning Ways"));
            if (metadata != null)
            {
                // free the raw graph and the intersection map,
                waySectioner = null;
                raw = null;
                analysis.freeIntersectionMap();

                // load vertex tags while we still have the location -> vertex map available,
                var vertexStore = store.vertexStore();
                vertexStore.postCommit(() -> vertexStore.loadVertexTags(dataSourceFactory().newInstance(destination.metadata()), tagFilter));

                // and return information about what we loaded.
                return metadata.withName(graphName);
            }
        }

        return null;
    }
}
