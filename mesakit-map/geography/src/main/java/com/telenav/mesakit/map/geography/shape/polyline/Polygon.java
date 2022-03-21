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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.polygon.PolygonSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.segment.SegmentRTreeSpatialIndex;
import com.telenav.mesakit.map.geography.lexakai.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.Outline;
import com.telenav.mesakit.map.geography.shape.Shape;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

@UmlClassDiagram(diagram = DiagramPolyline.class)
public class Polygon extends Polyline implements Shape
{
    public static final Collection<Polygon> EMPTY_SET = Collections.emptySet();

    public static Polygon fromLocationSequence(Iterable<Location> locations)
    {
        if (locations instanceof Polygon)
        {
            return (Polygon) locations;
        }
        var builder = new PolygonBuilder();
        builder.addAll(locations);
        return builder.build();
    }

    public static Polygon fromLocationSequence(Location one, Location two, Location three,
                                               Location... more)
    {
        var builder = new PolygonBuilder();
        builder.add(one);
        builder.add(two);
        builder.add(three);
        for (var location : more)
        {
            builder.add(location);
        }
        return builder.build();
    }

    /**
     * A {@link Outline} implementation that uses the segmentSpatialIndex() RTree to locate and test segments. If an odd
     * number of segments are intersected the location is contained, if an even number are intersected, the location is
     * outside the polygon.
     *
     * @author jonathanl (shibo)
     */
    public class SegmentShapeLocationIndex implements Outline
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Containment containment(Location location)
        {
            // TODO to correctly clean-cut edges, this method would need to handle the
            // Containment.ON_BORDER case. it presently doesn't do this.

            // Create a strip from the left side of the polygon to the location, that is 10 meters
            // high and contains the location and is also 5 meters left and right of the bounds to
            // take care of edge cases where the location is right on the edge of the polygon.
            var bounds = location.bounds().withLeft(bounds().left()).withRight(location.longitude())
                    .expanded(Distance.meters(5));

            // Create a horizontal from the left size of the polygon to the location
            var horizontal = new Segment(location.withLongitude(bounds.left()), location);

            // Count the number of intersections with segments and the horizontal line
            var intersections = 0;
            for (var segment : segmentSpatialIndex().intersecting(bounds))
            {
                // Determine which "region" (above or below the ray) each endpoint of the segment is
                // in (see http://idav.ucdavis.edu/~okreylos/TAship/Spring2000/PointInPolygon.html)
                var startAbove = segment.start().latitude().isGreaterThanOrEqualTo(location.latitude());
                var endAbove = segment.end().latitude().isGreaterThanOrEqualTo(location.latitude());

                // If the segment spans both regions and it intersects
                if (startAbove != endAbove && segment.intersects(horizontal))
                {
                    // increase intersection count
                    intersections++;
                }
            }

            // If there are an odd number of intersections, the location is inside the polygon
            return intersections % 2 == 1 ? Containment.INSIDE : Containment.OUTSIDE;
        }
    }

    private transient java.awt.Polygon polygon;

    private transient SegmentRTreeSpatialIndex segments;

    private Outline outline;

    private boolean fast;

    private Boolean clockwise;

    private List<Polygon> holes;

    public Polygon(List<Location> locations)
    {
        super(locations);
        ensure(segmentCountAsInteger() >= 3, "Not a polygon");
        ensure(isLoop(), "Polygon isn't closed");
    }

    public Polygon(long[] locations)
    {
        super(locations);
    }

    protected Polygon()
    {
    }

    public void addHole(Polygon hole)
    {
        if (holes == null)
        {
            holes = new ArrayList<>();
        }
        holes.add(hole);
    }

    public Bytes approximateSize()
    {
        return Bytes.primitiveSize(this);
    }

    public java.awt.Polygon asAwtPolygonInMicroDegrees()
    {
        if (polygon == null)
        {
            var size = size();
            var x = new int[size];
            var y = new int[size];
            for (var i = 0; i < size; i++)
            {
                x[i] = get(i).longitude().asMicrodegrees();
                y[i] = get(i).latitude().asMicrodegrees();
            }
            polygon = new java.awt.Polygon(x, y, size);
        }
        return polygon;
    }

    @Override
    public Containment containment(Location location)
    {
        if (bounds().containment(location).isInside())
        {
            var containment = outline().containment(location);
            if (containment != Containment.OUTSIDE)
            {
                if (holes != null)
                {
                    for (var hole : holes)
                    {
                        if (hole.contains(location))
                        {
                            return Containment.OUTSIDE;
                        }
                    }
                }
                return containment;
            }
        }
        return Containment.OUTSIDE;
    }

    public boolean contains(Polyline line)
    {
        if (!bounds().contains(line.bounds()))
        {
            return false;
        }
        for (var location : line)
        {
            if (!contains(location))
            {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Rectangle rectangle)
    {
        // The rectangle is fully contained if the bounds contains the rectangle fully,
        if (bounds().contains(rectangle))
        {
            // and there are no segments that intersect the rectangle
            if (intersections(rectangle, Count._1).isEmpty())
            {
                // and one of the corners is inside the rectangle (we know the other three corners
                // must be inside because no segments intersect the rectangle).
                return contains(rectangle.topLeft());
            }
        }
        return false;
    }

    public void fast(boolean fast)
    {
        this.fast = fast;
    }

    /**
     * Force creation of outline and segment spatial index
     */
    public void initialize()
    {
        outline();
    }

    /**
     * @return A list of up to the given maximum number segments intersecting the given bounds
     */
    public List<Segment> intersections(Rectangle bounds, Count maximum)
    {
        List<Segment> intersections = new ArrayList<>();
        if (size() < 100)
        {
            for (var segment : segments())
            {
                if (segment.intersects(bounds))
                {
                    intersections.add(segment);
                    if (intersections.size() == maximum.asInt())
                    {
                        break;
                    }
                }
            }
        }
        else
        {
            // Loop through segments that intersect the bounds
            for (var segment : segmentSpatialIndex().intersecting(bounds))
            {
                intersections.add(segment);
                if (intersections.size() == maximum.asInt())
                {
                    break;
                }
            }
        }
        return intersections;
    }

    public boolean intersectsOrContains(Polyline line)
    {
        for (var location : line)
        {
            if (contains(location))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isClockwise()
    {
        if (clockwise == null)
        {
            // This algorithm was found here:
            // http://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
            // It is based on the "Shoelace Formula" which is described here:
            // http://en.wikipedia.org/wiki/Shoelace_formula
            var sum = 0D;
            for (var segment : segments())
            {
                var x1 = segment.start().longitude().asDegrees();
                var y1 = segment.start().latitude().asDegrees();
                var x2 = segment.end().longitude().asDegrees();
                var y2 = segment.end().latitude().asDegrees();
                sum += (x2 - x1) * (y2 + y1);
            }
            clockwise = sum > 0;
        }
        return clockwise;
    }

    public boolean isCounterClockwise()
    {
        return !isClockwise();
    }

    /**
     * @return Lazily create the appropriate outline
     */
    public Outline outline()
    {
        if (outline == null)
        {
            // If we want the fast outline
            if (fast)
            {
                // we must first install segment shape index
                outline = new SegmentShapeLocationIndex();
                segmentSpatialIndex();

                // then replace it with the fast index
                outline = new PolygonSpatialIndex(this);
            }
            else
            {
                // slower, smaller outline implementation
                outline = new SegmentShapeLocationIndex();
                segmentSpatialIndex();
            }
        }
        return outline;
    }

    @Override
    public Count segmentCount()
    {
        return Count.count(size() - 1);
    }

    public SegmentRTreeSpatialIndex segmentSpatialIndex()
    {
        if (segments == null)
        {
            segments = new SegmentRTreeSpatialIndex("polygon.segments", segmentCount().asMaximum(), segments());
        }
        return segments;
    }
}
