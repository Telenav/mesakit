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

package com.telenav.aonia.map.geography.indexing.polygon;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.indexing.polygon.PolygonSpatialIndex.Visitor;
import com.telenav.aonia.map.geography.shape.Outline.Containment;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.geography.shape.segment.Segment;

import static com.telenav.aonia.map.geography.shape.Outline.Containment.INSIDE;
import static com.telenav.aonia.map.geography.shape.Outline.Containment.OUTSIDE;

/**
 * A leaf quadrant that intersects one or two connected segments of the polygon. A point on one side of the segment(s)
 * specifies the inside side of the segment such that a line between the inside point and a given point will cross one
 * of the segment(s) if the given point is outside the polygon (and won't cross either segment if the point is inside).
 *
 * @author jonathanl (shibo)
 */
public class Leaf extends Quadrant
{
    /**
     * The one or two segments that cross this leaf quadrant
     */
    private final Segment a, b;

    /**
     * The location specifying which side of the line is inside the polygon
     */
    private final Location inside;

    /**
     * @param a The first segment that intersects with the leaf
     * @param b Any (optional) second segment that intersects with the leaf
     */
    public Leaf(final Segment a, final Segment b, final Location inside)
    {
        // Save a and b and inside
        this.a = a;
        this.b = b;
        this.inside = inside;
    }

    public Segment a()
    {
        return a;
    }

    public Segment b()
    {
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Containment contains(final Location location, final Rectangle bounds)
    {
        // If there is no segment,
        if (a == null)
        {
            // then the entire leaf is contained
            return INSIDE;
        }

        // Create a test line from the inside location to the given location
        final var test = new Segment(inside(), location);

        // The location is contained by the polygon if the test line (from the inside location
        // to the given location) does not intersect either segment a or segment b (if b exists)
        final var contained = !a.intersects(test) && (b == null || !b.intersects(test));
        return contained ? INSIDE : OUTSIDE;
    }

    @Override
    public void debug(final PolygonSpatialIndexDebugger debugger, final Rectangle bounds)
    {
        debugger.leaf(bounds, a, b, inside);
    }

    public Location inside()
    {
        return inside;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visit(final Visitor visitor, final Rectangle bounds)
    {
        visitor.onLeaf(bounds, a, b, inside());
    }
}
