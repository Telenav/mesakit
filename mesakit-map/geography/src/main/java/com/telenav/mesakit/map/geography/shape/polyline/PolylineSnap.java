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

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.value.level.Level;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;

/**
 * The result of a polyline snap operation.
 *
 * @author matthieun
 */
@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolylineSnap extends Location
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Level offsetOnSegment;

    private final LocationSequence snappedOnto;

    private final Location source;

    private final Distance distanceToSource;

    private final int polylineIndex;

    /** Lazy generated */
    private Level offset;

    private Segment segment;

    private Heading segmentHeading;

    /**
     * Main constructor, extending Location
     *
     * @param polylineIndex The index of the segment in the polyline snappedOnto where the point has been snapped. This
     * starts with 0.
     */
    public PolylineSnap(Latitude latitude, Longitude longitude, LocationSequence snappedOnto,
                        Level offsetOnSegment, Location source, int polylineIndex)
    {
        super(latitude, longitude);
        this.offsetOnSegment = offsetOnSegment;
        this.snappedOnto = snappedOnto;
        this.source = source;
        this.polylineIndex = polylineIndex;
        distanceToSource = this.source.preciseDistanceTo(this);
    }

    /**
     * Copy from a Location
     *
     * @param polylineIndex The index of the segment in the polyline snappedOnto where the point has been snapped. This
     * starts with 0.
     */
    public PolylineSnap(Location location, LocationSequence snappedOnto, Level offset,
                        Location source, int polylineIndex)
    {
        this(location.latitude(), location.longitude(), snappedOnto, offset, source, polylineIndex);
    }

    /**
     * Returns the angle between the snap vector and the segment it snapped to
     */
    public Angle angle()
    {
        return vector().heading().difference(segment.heading(), Chirality.SMALLEST);
    }

    /**
     * Returns the distance between the source point, and the polyline
     */
    public Distance distanceToSource()
    {
        return distanceToSource;
    }

    /**
     * Returns true if the source point is on the left side of the directed segment
     */
    public boolean isLeft()
    {
        return !isRight();
    }

    /**
     * Returns true if the source point is on the right side of the directed segment
     */
    public boolean isRight()
    {
        return segmentHeading().difference(new Segment(this, source).heading(), Chirality.CLOCKWISE)
                .isLessThan(Angle._180_DEGREES);
    }

    /**
     * Returns the offset of the snap point on the whole polyline
     */
    public Level offset()
    {
        if (offset == null || Double.isNaN(offset.asZeroToOne()))
        {
            var polyline = Polyline.fromLocations(snappedOnto.locationSequence());
            var iterations = 0;
            Location previous = null;
            var traversed = Distance.ZERO;
            var polylineLength = polyline.length();
            for (var shapePoint : polyline)
            {
                iterations++;
                if (previous != null)
                {
                    var considered = new Segment(previous, shapePoint);
                    if (iterations == polylineIndex + 2)
                    {
                        var toAdd = considered.approximateLength().times(offsetOnSegment.asZeroToOne());
                        traversed = traversed.add(toAdd);
                    }
                    else
                    {
                        if (iterations < polylineIndex + 2)
                        {
                            traversed = traversed.add(considered.approximateLength());
                        }
                    }
                }
                previous = shapePoint;
            }
            var ratio = ((double) traversed.asMillimeters()) / ((double) polylineLength.asMillimeters());
            if (ratio - 1.0 > 0.0001 || 0.0 - ratio > 0.0001)
            {
                LOGGER.warning("Rounding up an offset ${debug} on a segment...", ratio);
            }

            if (Double.isNaN(ratio))
            {
                offset = new Level(0.0);
            }
            else
            {
                offset = new Level(ratio > 1.0 ? 1.0 : (Math.max(ratio, 0.0)));
            }
        }

        return offset;
    }

    /**
     * Returns the offset of the snapped point on the Segment in the polyline on which it has been snapped onto
     */
    public Level offsetOnSegment()
    {
        return offsetOnSegment;
    }

    public int polylineIndex()
    {
        return polylineIndex;
    }

    /**
     * Returns the segment in the polyline on which the point has been snapped
     */
    public Segment segment()
    {
        if (segment == null)
        {
            Location previous = null;
            var iterations = 0;
            for (var shapePoint : snappedOnto.locationSequence())
            {
                iterations++;
                if (previous != null && iterations == polylineIndex + 2)
                {
                    segment = new Segment(previous, shapePoint);
                }
                previous = shapePoint;
            }
            if (segment == null)
            {
                throw new IllegalStateException("Impossible to read polyline segment in " + iterations
                        + " iterations and polylineIndex =  " + polylineIndex);
            }
        }
        return segment;
    }

    /**
     * Returns the heading of the segment in the polyline on which the point has been snapped
     */
    public Heading segmentHeading()
    {
        if (segmentHeading == null)
        {
            segmentHeading = segment().heading();
        }
        return segmentHeading;
    }

    public Location source()
    {
        return source;
    }

    public LocationSequence target()
    {
        return snappedOnto;
    }

    public String toDebugString()
    {
        return "SnappedLocation [location=" + super.toString() + ", offsetOnSegment=" + offsetOnSegment
                + ", snappedOnto=" + snappedOnto + ", source=" + source + ", localHeading=" + segmentHeading
                + ", distanceToSource=" + distanceToSource + ", polylineIndex=" + polylineIndex + "]";
    }

    @Override
    public String toString()
    {
        return toDebugString();
    }

    /**
     * Returns the vector from the source to this snap location
     */
    public Segment vector()
    {
        return source().to(this);
    }
}
