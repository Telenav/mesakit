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

import com.telenav.kivakit.language.count.Count;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.polygon.PolygonSpatialIndex.Visitor;
import com.telenav.mesakit.map.geography.shape.Outline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.geography.shape.segment.SegmentPair;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;

import static com.telenav.mesakit.map.geography.indexing.polygon.PolygonSpatialIndex.MINIMUM_QUADRANT_SIZE;

/**
 * A interior {@link Node} of the spatial index that contains up to four quadrant objects, each of which could be
 * another {@link Node}, a {@link Leaf} or null (if the quadrant is outside the polygon).
 *
 * @author jonathanl (shibo)
 */
public class Node extends Quadrant
{
    // The 4 quadrants of this node
    private final int northEast;

    private final int northWest;

    private final int southEast;

    private final int southWest;

    private final PolygonSpatialIndex spatialIndex;

    /**
     * Used when reconstructing quadrants from the quadrant store
     */
    public Node(PolygonSpatialIndex index, int northEast, int northWest, int southEast,
                int southWest)
    {
        spatialIndex = index;
        this.northEast = northEast;
        this.northWest = northWest;
        this.southEast = southEast;
        this.southWest = southWest;
    }

    /**
     * @param bounds The bounding rectangle of this quadrant
     */
    public Node(PolygonSpatialIndex spatialIndex, Rectangle bounds)
    {
        this.spatialIndex = spatialIndex;

        // Recursively create indexes for each of the 4 quadrants of this node
        northEast = index(bounds.northEastQuadrant());
        northWest = index(bounds.northWestQuadrant());
        southEast = index(bounds.southEastQuadrant());
        southWest = index(bounds.southWestQuadrant());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Outline.Containment contains(Location location, Rectangle bounds)
    {
        // If there is a NE quadrant,
        if (northEast != 0)
        {
            // and the location is inside the bounds for it,
            var northEastQuadrant = bounds.northEastQuadrant();
            if (northEastQuadrant.contains(location))
            {
                // return the result of searching that quadrant for the location
                return northEast().contains(location, northEastQuadrant);
            }
        }

        // If there is a NW quadrant,
        if (northWest != 0)
        {
            // and the location is inside the bounds for it,
            var northWestQuadrant = bounds.northWestQuadrant();
            if (northWestQuadrant.contains(location))
            {
                // return the result of searching that quadrant for the location
                return northWest().contains(location, northWestQuadrant);
            }
        }

        // If there is a SE quadrant,
        if (southEast != 0)
        {
            // and the location is inside the bounds for it,
            var southEastQuadrant = bounds.southEastQuadrant();
            if (southEastQuadrant.contains(location))
            {
                // return the result of searching that quadrant for the location
                return southEast().contains(location, southEastQuadrant);
            }
        }

        // If there is a SW quadrant,
        if (southWest != 0)
        {
            // and the location is inside the bounds for it,
            var southWestQuadrant = bounds.southWestQuadrant();
            if (southWestQuadrant.contains(location))
            {
                // return the result of searching that quadrant for the location
                return southWest().contains(location, southWestQuadrant);
            }
        }

        // If the quad tree exceeded the minimum quadrant size and inclusion still could not be determined
        // we return INDETERMINATE, otherwise the location is OUTSIDE
        return bounds.heightAsDistance().isLessThanOrEqualTo(MINIMUM_QUADRANT_SIZE) ? Outline.Containment.INDETERMINATE : Outline.Containment.OUTSIDE;
    }

    @Override
    public void debug(PolygonSpatialIndexDebugger debugger, Rectangle bounds)
    {
        if (northEast != 0)
        {
            debugger.quadrant(bounds.northEastQuadrant());
        }
        if (northWest != 0)
        {
            debugger.quadrant(bounds.northWestQuadrant());
        }
        if (southEast != 0)
        {
            debugger.quadrant(bounds.southEastQuadrant());
        }
        if (southWest != 0)
        {
            debugger.quadrant(bounds.southWestQuadrant());
        }
    }

    public int northEastIndex()
    {
        return northEast;
    }

    public int northWestIndex()
    {
        return northWest;
    }

    public int southEastIndex()
    {
        return southEast;
    }

    public int southWestIndex()
    {
        return southWest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visit(Visitor visitor, Rectangle bounds)
    {
        if (northEast != 0)
        {
            northEast().visit(visitor, bounds.northEastQuadrant());
        }
        if (northWest != 0)
        {
            northWest().visit(visitor, bounds.northWestQuadrant());
        }
        if (southEast != 0)
        {
            southEast().visit(visitor, bounds.southEastQuadrant());
        }
        if (southWest != 0)
        {
            southWest().visit(visitor, bounds.southWestQuadrant());
        }
    }

    Quadrant northEast()
    {
        return spatialIndex.store().get(northEast);
    }

    Quadrant northWest()
    {
        return spatialIndex.store().get(northWest);
    }

    Quadrant southEast()
    {
        return spatialIndex.store().get(southEast);
    }

    Quadrant southWest()
    {
        return spatialIndex.store().get(southWest);
    }

    /**
     * @return The {@link Node} or {@link Leaf} quadrant that indexes the given bounding rectangle
     */
    private int index(Rectangle bounds)
    {
        // Find up to 3 intersections between the given bounding rectangle and the complete list
        // of polygon segments. This is time consuming but only has to be done once when
        // building the spatial index so we don't care too much about optimizing it.
        var intersections = spatialIndex.polygon().intersections(bounds, Count._3);

        // Switch on the number of intersections we found
        switch (intersections.size())
        {
            case 0:
            {
                // No intersections were found, so if the polygon completely contains the
                // quadrant bounding rectangle,
                if (spatialIndex.polygon().contains(bounds))
                {
                    // then we have a leaf that's completely inside the polygon
                    return spatialIndex.store().add(new Leaf(null, null, null));
                }

                // The quadrant is completely outside the polygon
                return 0;
            }

            case 1:
            {
                // A single intersection was found, so we have a leaf with just one segment
                var a = intersections.iterator().next();
                return spatialIndex.store().add(new Leaf(a, null, inside(a, null)));
            }

            case 2:
            {
                // Two intersections were found, so get those two segments
                var iterator = intersections.iterator();
                var a = iterator.next();
                var b = iterator.next();

                // If a leads to b
                if (a.leadsTo(b))
                {
                    // then we have a leaf with two connected segments a->b
                    return spatialIndex.store().add(new Leaf(a, b, inside(a, b)));
                }

                // If b leads to a
                if (b.leadsTo(a))
                {
                    // then we have a leaf with two connected segments b->a
                    return spatialIndex.store().add(new Leaf(b, a, inside(b, a)));
                }

                // otherwise we need to break things down further
                if (bounds.widthAtBase().isGreaterThan(MINIMUM_QUADRANT_SIZE)
                        && bounds.heightAsDistance().isGreaterThan(MINIMUM_QUADRANT_SIZE))
                {
                    return spatialIndex.store().add(new Node(spatialIndex, bounds));
                }
                return 0;
            }

            case 3:
            {
                // If the bounds is not too small
                if (bounds.widthAtBase().isGreaterThan(MINIMUM_QUADRANT_SIZE)
                        && bounds.heightAsDistance().isGreaterThan(MINIMUM_QUADRANT_SIZE))
                {
                    // break down the quadrant further
                    return spatialIndex.store().add(new Node(spatialIndex, bounds));
                }

                // Bounding box is too small to break down further
                return 0;
            }

            default:
                throw new IllegalStateException();
        }
    }

    private Location inside(Segment a, Segment b)
    {
        // If we have no segment a
        if (a == null)
        {
            // we're a fully included leaf
            return null;
        }
        else
        {
            // if we have an a, but no segment b
            if (b == null)
            {
                // then we locate the inside point a short distance perpendicular to the line
                var perpendicular = spatialIndex.polygon().isClockwise() ? Angle._90_DEGREES
                        : Angle._270_DEGREES;
                return a.center().moved(a.heading().plus(perpendicular), PolygonSpatialIndex.INSIDE_OFFSET);
            }
            else
            {
                // Create a segment pair such that it's organized as a pair of clock hands (the
                // shared point is the start of each segment)
                var pair = new SegmentPair(a, b).asClockHands();
                if (pair != null)
                {
                    // Bisect the heading of the two clock hands
                    var clockCenter = pair.first().start();
                    var bisected = pair.bisect(
                            spatialIndex.polygon().isClockwise() ? Chirality.COUNTERCLOCKWISE : Chirality.CLOCKWISE);

                    // and pick a point a short distance away
                    return clockCenter.moved(bisected, PolygonSpatialIndex.INSIDE_OFFSET);
                }
                else
                {
                    return null;
                }
            }
        }
    }
}
