////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.geography.indexing.polygon;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramSpatialIndex;
import com.telenav.mesakit.map.geography.shape.Outline;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * A {@link PolygonSpatialIndex} indexes a polygon with a quad-tree-like structure that allows for fast containment
 * testing with {@link #containment(Location)}.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramSpatialIndex.class)
public class PolygonSpatialIndex implements Intersectable, Bounded, Outline
{
    // The offset from the segment(s) to use for the "inside" location in Leaf nodes
    static final Distance INSIDE_OFFSET = Distance.ONE_METER;

    static final Distance MINIMUM_QUADRANT_SIZE = Distance.meters(250);

    public interface Visitor
    {
        void onLeaf(Rectangle bounds, Segment a, Segment b, Location inside);
    }

    /**
     * The polygon we're indexing
     */
    private transient Polygon polygon;

    /**
     * Bounding rectangle of the polygon
     */
    private Rectangle bounds;

    /**
     * The quad-tree index
     */
    private int root;

    /**
     * Compressed quadrant store
     */
    private QuadrantStore store;

    /**
     * @param polygon The polygon to index
     */
    public PolygonSpatialIndex(final Polygon polygon)
    {
        // Create quadrant store
        store = new QuadrantStore(this);

        // Save the polygon and it's bounds
        this.polygon = polygon;
        bounds = polygon.asBoundsFromOrigin();

        // Recursively construct the spatial index
        root = store.add(new Node(this, bounds));

        // No longer need the polygon since we've built the index
        this.polygon = null;
    }

    protected PolygonSpatialIndex()
    {
    }

    @Override
    public Rectangle asBoundsFromOrigin()
    {
        return bounds;
    }

    /**
     * @return True if this spatially indexed polygon contains the given location.
     */
    @Override
    public Containment containment(final Location location)
    {
        if (bounds.contains(location))
        {
            return rootQuadrant().contains(location, bounds);
        }
        return Containment.OUTSIDE;
    }

    public void debug(final PolygonSpatialIndexDebugger debugger)
    {
        rootQuadrant().debug(debugger, bounds);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof PolygonSpatialIndex)
        {
            final var that = (PolygonSpatialIndex) object;
            return bounds.equals(that.bounds);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return bounds.hashCode();
    }

    @Override
    public boolean intersects(final Rectangle bounds)
    {
        return this.bounds.intersects(bounds);
    }

    /**
     * @param visitor The visitor callback that should be used in traversing this spatial index
     */
    public void visit(final Visitor visitor)
    {
        rootQuadrant().visit(visitor, bounds);
    }

    Polygon polygon()
    {
        return polygon;
    }

    Quadrant rootQuadrant()
    {
        return store.get(root);
    }

    QuadrantStore store()
    {
        return store;
    }
}
