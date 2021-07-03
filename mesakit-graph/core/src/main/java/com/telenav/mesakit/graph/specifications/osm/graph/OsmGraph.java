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

package com.telenav.mesakit.graph.specifications.osm.graph;

import com.telenav.kivakit.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.MutableCount;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.analytics.junction.JunctionEdgeFinder;
import com.telenav.mesakit.graph.analytics.junction.JunctionEdgeOptimizer;
import com.telenav.mesakit.graph.analytics.ramp.RampFinder;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.specifications.common.CommonGraph;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.OsmEdge;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.road.model.RoadSubType;

public final class OsmGraph extends CommonGraph
{
    public OsmGraph(final Metadata metadata)
    {
        super(metadata);
    }

    /**
     * Forces each edge in this graph to determine and possibly cache its double-digitization status
     */
    public final void markDoubleDigitizedEdges()
    {
        if (supports(EdgeAttributes.get().ROAD_NAMES))
        {
            information(AsciiArt.topLine(20, "Marking double-digitized edges"));
            final var start = Time.now();
            final var count = new MutableCount();
            final var progress = isDeaf() ? Progress.NULL : Progress.create(this, "edges");
            progress.steps(edgeCount().asMaximum());
            progress.start();
            for (final var edge : forwardEdges())
            {
                if (((OsmEdge) edge).computeDoubleDigitized(Angle.degrees(50)))
                {
                    count.increment();
                }
                progress.next();
            }
            progress.end();
            information(AsciiArt.bottomLine(20, "Marked $ double-digitized edges in ${debug}.", count, start.elapsedSince()));
        }
    }

    public final void markEdges()
    {
        if (metadata().dataSupplier().isOsm())
        {
            markDoubleDigitizedEdges();
            markRampsAndConnectors();
            markJunctionEdges();
        }
    }

    public void markJunctionEdges()
    {
        information(AsciiArt.topLine(20, "Marking junction edges"));
        final var junctionEdges = optimizeJunctionEdges(findJunctionEdges());
        storeJunctionEdges(junctionEdges);
        information(AsciiArt.bottomLine(20, "Marked $ junction edges", junctionEdges.size()));
    }

    public void markRampsAndConnectors()
    {
        final var ramps = new MutableCount();
        information(AsciiArt.topLine(20, "Marking ramps and connectors"));
        final var finder = listenTo(new RampFinder(this)
        {
            @Override
            protected void onRamp(final Edge ramp)
            {
                // Edge is already a ramp so do nothing
            }

            @Override
            protected void onRampConnector(final Edge edge)
            {
                // Make this ramp connector edge a proper ramp
                edgeStore().storeRoadSubType(edge, RoadSubType.RAMP);
                ramps.increment();
            }
        });
        finder.find();
        information(AsciiArt.bottomLine(20, "Marked $ ramps", ramps));
    }

    private EdgeSet findJunctionEdges()
    {
        final var start = Time.now();
        final var connectors = new MutableCount();
        final var junctions = new EdgeSet();
        final var finder = new JunctionEdgeFinder(edges())
        {
            @Override
            protected void onConnector(final Edge edge)
            {
                edgeStore().storeRoadSubType(edge, RoadSubType.CONNECTING_ROAD);
                connectors.increment();
            }

            @Override
            protected void onJunction(final Edge edge)
            {
                junctions.add(edge);
            }
        };
        finder.addListener(this);
        finder.find();
        information("Marked $ junction edges and $ connectors in $", junctions.size(), connectors, start.elapsedSince());
        return junctions;
    }

    private EdgeSet optimizeJunctionEdges(final EdgeSet edges)
    {
        final var start = Time.now();
        final var optimized = new JunctionEdgeOptimizer(edges).optimize();
        information("Optimized $ junction edges in $", optimized.size(), start.elapsedSince());
        return optimized;
    }

    private void storeJunctionEdges(final EdgeSet junctions)
    {
        for (final var edge : junctions)
        {
            edgeStore().storeRoadSubType(edge, RoadSubType.INTERSECTION_LINK);
        }
    }
}