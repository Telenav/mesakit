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

package com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner;

import com.telenav.kivakit.collections.map.MultiMap;
import com.telenav.kivakit.language.count.Count;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.specifications.library.pbf.IntersectionMap;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataAnalysis;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineSectioner;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.telenav.kivakit.ensure.Ensure.ensure;

/**
 * Sections a raw way (represented as an edge) into smaller pieces according to the following rules:
 * <ul>
 * <li>Break the edge at all intersections</li>
 * <li>Break the edge at intervals if it's too long</li>
 * <li>Bisect edges that form ambiguous connections between the same two vertexes</li>
 * <li>Trisect loops</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
public class EdgeSectioner extends BaseRepeater
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /** The maximum edge length for sectioning long ways */
    private final Distance maximumEdgeLength;

    /** The destination graph */
    private final Graph destination;

    /** Map from edge to list of nodes from raw graph loader */
    private final EdgeNodeMap edgeNodeMap;

    /** Intersections to section at */
    private final IntersectionMap intersections;

    private EdgeSectionerDebugger debugger;

    private Set<PbfWayIdentifier> waysToDebug;

    public EdgeSectioner(Graph destination, PbfDataAnalysis analysis,
                         EdgeNodeMap edgeNodeMap, Distance maximumEdgeLength)
    {
        this.destination = destination;
        this.edgeNodeMap = edgeNodeMap;
        this.maximumEdgeLength = maximumEdgeLength;
        intersections = analysis.intersections();

        ensure(intersections != null);
    }

    public void debugger(EdgeSectionerDebugger debugger, Set<PbfWayIdentifier> waysToDebug)
    {
        this.debugger = debugger;
        this.waysToDebug = waysToDebug;
    }

    /**
     * @return A list of sectioned edges for the given edge
     */
    public List<Edge> section(Edge edge)
    {
        // Get nodes list for way
        var nodes = edgeNodeMap.get(edge.identifier());

        // If there are at least two nodes we can do way sectioning
        if (nodes != null && nodes.size() >= 2)
        {
            // Create a single edge section for the parent edge (the way)
            var parent = new EdgeSection(edge, nodes, edge.roadShape());

            // If the edge we're sectioning is the specific way we want to debug
            if (debugger != null && waysToDebug.contains(edge.wayIdentifier()))
            {
                // then turn on visual debugging for this particular edge ONLY and turn it off in
                // the finally clause below
                debugger.start();
                debugger.update(parent);
            }

            try
            {
                // Break edge at intersections, simple loops, split ambiguous edges and finally
                // section any edges that are too long
                return sectionLongEdges(sectionAmbiguousEdges(sectionLoops(sectionAtIntersections(parent)))).edges();
            }
            catch (Exception e)
            {
                LOGGER.warning(e, "Unable to section edge $ of $", edge.identifier(), edge.graph().name());
            }
            finally
            {
                // Stop any debugging we might be doing
                if (debugger != null)
                {
                    debugger.stop();
                }
            }
        }

        return Collections.singletonList(edge);
    }

    /**
     * @return The given list of edge sections with any ambiguities resolved by bisecting a way
     */
    private EdgeSectionList sectionAmbiguousEdges(EdgeSectionList sections)
    {
        var connections = new MultiMap<Segment, EdgeSection>();
        Set<EdgeSection> ambiguous = new HashSet<>();

        // Go through the given way sections
        for (var section : sections)
        {
            // add a mapping from the section as a segment to the section
            connections.add(section.asSegment(), section);

            // and if the section's from and to are already connected (by an edge added prior)
            if (destination.vertexStore().internalIsConnected(section.from(), section.to()))
            {
                // then the section is ambiguous.
                ambiguous.add(section);
            }
        }

        // If the segment is ambiguous OR there is a redundant connection in the given sections
        if (!ambiguous.isEmpty() || connections.maximumListSize().isGreaterThan(Count._1))
        {
            // then we need to split each ambiguous edge in the list of sections.
            var sectioned = new EdgeSectionList();
            for (var section : sections)
            {
                // If the section is ambiguous,
                if (ambiguous.contains(section) || connections.get(section.asSegment()).size() > 1)
                {
                    // then add the section in two bisected pieces,
                    sectioned.addAll(section.bisect());
                }
                else
                {
                    // otherwise, just add it whole.
                    sectioned.add(section);
                }
            }

            return sectioned;
        }

        // There was no ambiguity
        return sections;
    }

    /**
     * @return A list of edge sections for the given section, broken at intersections
     */
    private EdgeSectionList sectionAtIntersections(EdgeSection section)
    {
        // Section the edge's road shape at intersections
        var sectioner = new PolylineSectioner(section.shape());
        for (var index = 1; index < section.nodes().size() - 1; index++)
        {
            if (intersections.isIntersection(section.nodes().get(index).asLong()))
            {
                sectioner.cutAtIndex(index);
            }
        }

        // Return edge sections for polyline sections
        return section.section(sectioner);
    }

    private EdgeSectionList sectionLongEdges(EdgeSectionList sections)
    {
        // If we are supposed to section long edges,
        if (maximumEdgeLength.isLessThan(Distance.MAXIMUM))
        {
            // Sectioned edges to return
            var sectioned = new EdgeSectionList();

            // Go through sections
            for (var section : sections)
            {
                // If the section is too long
                if (section.length().isGreaterThan(maximumEdgeLength))
                {
                    // break the section's shape into pieces of the maximum length
                    for (var polylineSection : section.shape().sections(maximumEdgeLength))
                    {
                        // and add each piece as a new edge section
                        sectioned.add(section.section(polylineSection));
                    }
                }
                else
                {
                    // This particular section isn't too long
                    if (debugger != null)
                    {
                        debugger.update(section);
                    }
                    sectioned.add(section);
                }
            }

            return sectioned;
        }
        else
        {
            // No need to section
            return sections;
        }
    }

    private EdgeSectionList sectionLoops(EdgeSectionList sections)
    {
        // Sections to return
        var sectioned = new EdgeSectionList();

        // Go through each section
        for (var section : sections)
        {
            // and if it's a loop,
            if (section.shape().isLoop())
            {
                // trisect the loop
                sectioned.addAll(section.trisect());
            }
            else
            {
                // otherwise, just add the section
                sectioned.add(section);
                if (debugger != null)
                {
                    debugger.update(section);
                }
            }
        }
        return sectioned;
    }
}
