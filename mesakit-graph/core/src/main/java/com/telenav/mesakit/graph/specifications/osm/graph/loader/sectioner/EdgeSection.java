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

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineSection;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineSectioner;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an edge that has been cut from a larger way as part of the edge sectioning process.
 *
 * @author jonathanl (shibo)
 */
public class EdgeSection
{
    /** Parent edge from which this section was cut */
    private final Edge edge;

    /** List of node identifiers in this section */
    private final List<MapNodeIdentifier> nodes;

    /** The shape of this section */
    private final Polyline shape;

    /**
     * @param edge The edge for which this is a section
     * @param nodes List of nodes for this edge section
     * @param shape The {@link Polyline} for this edge section
     */
    public EdgeSection(Edge edge, List<MapNodeIdentifier> nodes, Polyline shape)
    {
        assert edge != null;
        assert nodes != null;
        assert shape != null;

        this.edge = edge;
        this.nodes = nodes;
        this.shape = shape;

        // Validate there are at least two nodes and locations
        assert nodes.size() >= 2;
        assert shape.size() >= 2;

        // Validate that there are the same number of nodes and locations
        assert nodes.size() == shape.size() :
                "Edge " + edge.identifier() + ": " + nodes.size() + " nodes != " + shape.size() + " locations";
    }

    /**
     * @return A single segment connecting the start of this section to the end
     */
    public Segment asSegment()
    {
        var shape = shape();
        return new Segment(shape.start(), shape.end());
    }

    public EdgeSectionList bisect()
    {
        var sections = new EdgeSectionList();

        // If we're bisecting a segment
        if (size() == 2)
        {
            // create a synthetic OSM node identifier for the bisection point
            var syntheticIdentifier = PbfNodeIdentifier.nextSyntheticNodeIdentifier();

            // add first segment to the bisection point
            List<MapNodeIdentifier> firstNodes = new ArrayList<>();
            firstNodes.add(nodes.get(0));
            firstNodes.add(syntheticIdentifier);

            var firstShape = new PolylineBuilder();
            firstShape.add(shape.start());
            firstShape.add(asSegment().midpoint());

            sections.add(new EdgeSection(edge(), firstNodes, firstShape.build()));

            // add second segment from the bisection point
            List<MapNodeIdentifier> secondNodes = new ArrayList<>();
            secondNodes.add(syntheticIdentifier);
            secondNodes.add(nodes.get(1));

            var secondShape = new PolylineBuilder();
            secondShape.add(asSegment().midpoint());
            secondShape.add(shape.end());

            sections.add(new EdgeSection(edge(), secondNodes, secondShape.build()));
        }
        else
        {
            // Bisect at existing OSM node
            for (var section : shape.bisect())
            {
                sections.add(section(section));
            }
        }
        return sections;
    }

    public Edge edge()
    {
        return edge;
    }

    /**
     * @return A copy of the parent edge that's been modified to have the 'from' and 'to' nodes and road shape of this
     * edge section.
     */
    public HeavyWeightEdge edge(EdgeIdentifier identifier)
    {
        // Create a new temporary edge that's a copy of the parent edge for this section
        var section = edge.graph().newHeavyWeightEdge(identifier);

        // Copy the attributes of the parent edge
        section.copy(edge);

        // Set the road shape to our portion of the parent (we do it here so that copy won't copy
        // the parent road shape needlessly, which can be expensive because the road shape is
        // compressed)
        section.roadShapeAndLength(shape, shape.start(), shape().end());

        // Set the 'from' and 'to' node identifiers
        section.fromNodeIdentifier(from());
        section.toNodeIdentifier(to());

        // Set the clipped state to true only for vertexes that are at the ends of the parent edge
        var fromClipped = shape.start().asDm5Long() == edge.fromLocation().asDm5Long() && edge.from().isClipped();
        var toClipped = shape.end().asDm5Long() == edge.toLocation().asDm5Long() && edge.to().isClipped();
        section.fromVertexClipped(fromClipped);
        section.toVertexClipped(toClipped);

        // Remove the vertexes copied from the parent edge since they will be determined after graph
        // sectioning is complete (see Edge.fromLocation() for details)
        section.from(null);
        section.to(null);

        return section;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof EdgeSection)
        {
            var that = (EdgeSection) object;
            return this == that;
        }
        return false;
    }

    public MapNodeIdentifier from()
    {
        return nodes().get(0);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    public Distance length()
    {
        return shape.length();
    }

    public List<MapNodeIdentifier> nodes()
    {
        return nodes;
    }

    /**
     * @return A sub-section of this edge section given a section of a {@link Polyline}
     */
    public EdgeSection section(PolylineSection section)
    {
        return new EdgeSection(edge(), nodes.subList(section.fromIndex(), section.toIndex() + 1), section.shape());
    }

    /**
     * @return This edge section broken into pieces by the given sectioner
     */
    public EdgeSectionList section(PolylineSectioner sectioner)
    {
        var sections = new EdgeSectionList();
        for (var section : sectioner.sections())
        {
            sections.add(section(section));
        }
        return sections;
    }

    public Polyline shape()
    {
        return shape;
    }

    public int size()
    {
        return nodes.size();
    }

    public MapNodeIdentifier to()
    {
        return nodes().get(size() - 1);
    }

    @Override
    public String toString()
    {
        return "[EdgeSection identifier = " + edge.identifier() + ", nodes = " + nodes + ", shape = "
                + shape + "]";
    }

    public EdgeSectionList trisect()
    {
        var sections = new EdgeSectionList();
        for (var section : shape.trisect())
        {
            sections.add(section(section));
        }
        return sections;
    }
}
