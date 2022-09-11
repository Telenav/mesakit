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

import com.telenav.kivakit.core.value.level.Level;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Heading;

/**
 * Snaps a point to a polyline (or list of points defining a list of segments)
 *
 * @author matthieun
 */
@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolylineSnapper
{
    /**
     * Snap a point to a polyline
     *
     * @param polyline The list of locations forming a polyline
     * @param point The point to snap
     * @return The point snapped to the polyline
     */
    @UmlRelation(label = "calculates")
    public PolylineSnap snap(LocationSequence polyline, Location point)
    {
        return snap(polyline, point, null);
    }

    /**
     * Snap a point to a polyline
     *
     * @param polyline The list of locations forming a polyline
     * @param point The source point to snap
     * @param headingAtPoint The heading of the point to help with disambiguation
     * @return The point snapped to the polyline
     */
    public PolylineSnap snap(LocationSequence polyline, Location point, Heading headingAtPoint)
    {
        Location previous = null;
        PolylineSnap best = null;
        Heading bestDifference = null;
        var iterations = 0;
        var indexOnPolyline = 0;

        for (var location : polyline.locationSequence())
        {
            /* Loop through the Segments in this iteration of locations */
            iterations++;
            if (previous != null)
            {
                var segment = new Segment(previous, location);
                var snappedLocation = planarOrthogonalProjectionWithCartesianIndexing(segment, point);
                if (best != null)
                {
                    /* Give more importance to a closer snap */
                    if (snappedLocation.distanceToSource().isLessThan(best.distanceToSource()))
                    {
                        best = snappedLocation;
                        indexOnPolyline = iterations - 2;
                        bestDifference = headingAtPoint != null ? headingAtPoint.minus(best.segmentHeading()) : null;
                    }
                    else
                    {
                        /*
                          If snaps are as far away, give more importance to the one that has the
                          closest heading
                         */
                        var difference = headingAtPoint != null
                                ? headingAtPoint.minus(snappedLocation.segmentHeading())
                                : null;
                        if (headingAtPoint != null && snappedLocation.distanceToSource().equals(best.distanceToSource())
                                && difference.isLessThan(bestDifference))
                        {
                            best = snappedLocation;
                            indexOnPolyline = iterations - 2;
                            bestDifference = difference;
                        }
                    }
                }
                else
                {
                    best = snappedLocation;
                    indexOnPolyline = iterations - 2;
                    bestDifference = headingAtPoint != null ? headingAtPoint.minus(best.segmentHeading()) : null;
                }
            }
            previous = location;
        }
        if (iterations == 0)
        {
            /* If there are no locations to iterate over */
            return null;
        }
        if (iterations == 1)
        {
            /* If there is only one Location in the iteration, the snap result is obvious */
            return new PolylineSnap(previous, new Segment(previous, previous), new Level(0.0), point, 0);
        }

        /* provide the result with the correct index on the polyline. */
        return best == null ? null : new PolylineSnap(best, polyline, best.offsetOnSegment(), best.source(), indexOnPolyline);
    }

    /**
     * Orthogonal snap on a segment, with Cartesian indexing.
     *
     * @param there The {@link Location} to snap to the {@link Segment}
     * @return The {@link PolylineSnap} on the {@link Segment}
     */
    private PolylineSnap planarOrthogonalProjectionWithCartesianIndexing(Segment segment, Location there)
    {

        /*
          The abscissa of the start of the segment = origin of the referential
         */
        var xZero = segment.start().longitude().asDegrees();

        /*
          The ordinate of the start of the segment = origin of the referential
         */

        var yZero = segment.start().latitude().asDegrees();

        /*
          The normalization coefficient to take into account the fact that delta longitudes
          equivalent distance get skewed smaller at higher latitudes. We then normalize longitudes
          to have a normalized planar space
         */
        var normalizationCoefficient = Math.cos(segment.start().latitude().asRadians());

        /*
          The abscissa of the end of the segment
         */
        var xb = (segment.end().longitude().asDegrees() - xZero) * normalizationCoefficient;

        /*
          The ordinate of the end of the segment
         */
        var yb = segment.end().latitude().asDegrees() - yZero;

        /*
          The abscissa of the point to snap
         */
        var xp = (there.longitude().asDegrees() - xZero) * normalizationCoefficient;

        /*
          The ordinate of the point to snap
         */
        var yp = there.latitude().asDegrees() - yZero;

        /*
          The abscissa, ordinate of the snapped point, and the offset on the segment from the start
          point
         */
        double xs, ys, offset;

        /*
          Special case: If we have a segment that spans along no delta of longitude (segment
          pointing north to south)
         */
        if (xb == 0.0)
        {
            /*
              Special case: If we have a segment of size 0: the snap result = start = end.
             */
            if (yb == 0.0)
            {
                return new PolylineSnap(segment.start(), segment, new Level(0.0), there, 0);
            }

            /*
              The abscissa of the snapped point is 0, and its ordinate is the ordinate of the point
              to snap.
             */
            xs = 0;
            ys = yp;

            /*
              The offset is the ratio of snapped point's ordinate versus the end point's ordinate
             */
            offset = ys / yb;
        }
        else
        {
            /*
              The slope of the segment in an approximate plan. The end point is following the
              segment's equation in the plan. y = alpha * x + beta, with beta = 0 here because the
              start point of the segment is the origin. This leads us to the slope of the segment
              defined by the end point's coordinates only.
             */
            var alpha = yb / xb;

            /*
              The abscissa of the matched point, obtained by using the fact that the slope of the
              segment between the point to snap and its snapped point (orthogonal segment) is the
              opposite of the inverse of the slope of the segment to snap onto. theta = - 1 / alpha.
              Replacing theta in the orthogonal segment's equation allows us to isolate the snapped
              point's abscissa, with respect to the point to snap's ordinate and the main segment's
              end point's abscissa.
             */
            xs = (xp + (alpha * yp)) / ((alpha * alpha) + 1);

            /*
              The ordinate of the matched point (on the segment) is following the segment's
              equation in the plan. y = alpha * x + beta, with beta = 0 here because the start
              point of the segment is the origin.
             */
            ys = alpha * xs;

            /*
              The offset from the start point is the ratio of snapped point's abscissa versus the
              end point's abscissa
             */
            offset = xs / xb;
        }

        /*
          Corner case: we snapped further away than the end of the segment.
         */
        if (offset >= 1.0)
        {
            return new PolylineSnap(segment.end(), segment, new Level(1.0), there, 0);
        }

        /*
          Corner case: we snapped further away than the start of the segment.
         */
        if (offset <= 0.0)
        {
            return new PolylineSnap(segment.start(), segment, new Level(0.0), there, 0);
        }

        /*
          De-normalize to come back to lat/lon
         */
        var longitudeXs = xs / normalizationCoefficient;

        return new PolylineSnap(Latitude.degrees(yZero + ys), Longitude.degrees(xZero + longitudeXs), segment,
                new Level(offset), there, 0);
    }
}
