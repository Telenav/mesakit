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

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.lexakai.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplify a {@link Polyline} using the Ramer Douglas Peucker algorithm
 *
 * @author matthieun
 * @see <a href="http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer
 * Douglas Peucker</a>
 */
@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolylineSimplifier
{
    /**
     * Recursively simplify a polyline using the Ramer Douglas Peucker algorithm
     *
     * @param polyline The {@link Polyline} to simplify
     * @param tolerance The distance tolerance for the simplification
     * @return The simplified list of points
     * @see <a href= "http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer Douglas
     * Peucker</a>
     */
    @UmlRelation(label = "simplifies")
    public Polyline simplify(Polyline polyline, Distance tolerance)
    {
        var simplified = simplify(new ArrayList<>(polyline.locations()), tolerance);
        return Polyline.fromLocations(simplified);
    }

    /**
     * Recursively simplify a polyline using the Ramer Douglas Peucker algorithm
     *
     * @param points The list of points to simplify.
     * @param tolerance The distance tolerance for the simplification
     * @return The simplified list of points
     * @see <a href= "http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer Douglas
     * Peucker</a>
     */
    private List<Location> simplify(List<Location> points, Distance tolerance)
    {
        // Terminal Case
        if (points.size() <= 2)
        {
            return points;
        }
        // Weird case, when we have a loop
        if (points.get(0).equals(points.get(points.size() - 1)))
        {
            var size = points.size();
            var points1 = points.subList(0, size / 2 + 1);
            var points2 = points.subList(size / 2, size - 1);
            // split in 2 and simplify each side
            return simplifyAndConcatenate(points1, points2, tolerance);
        }
        // Non-Terminal Case
        var size = points.size();
        var segment = new Segment(points.get(0), points.get(size - 1));
        Distance maximumSnapDistance = null;
        var indexOfFarthestPoint = -1;
        var snapper = new PolylineSnapper();
        // Find the farthest point
        for (var i = 1; i < size - 1; i++)
        {
            var candidate = points.get(i);
            var snapDistance = snapper.snap(segment, candidate).distanceToSource();
            if (maximumSnapDistance != null)
            {
                if (snapDistance.isGreaterThan(maximumSnapDistance))
                {
                    maximumSnapDistance = snapDistance;
                    indexOfFarthestPoint = i;
                }
            }
            else
            {
                maximumSnapDistance = snapDistance;
                indexOfFarthestPoint = i;
            }
        }
        if (maximumSnapDistance.isGreaterThanOrEqualTo(tolerance))
        {
            var points1 = points.subList(0, indexOfFarthestPoint + 1);
            var points2 = points.subList(indexOfFarthestPoint, size);
            // Keep the point and recurse when applicable
            return simplifyAndConcatenate(points1, points2, tolerance);
        }
        else
        {
            // Remove all the points together and keep the Segment, because they must all be
            // within the tolerance
            List<Location> result = new ArrayList<>();
            result.add(segment.start());
            result.add(segment.end());
            return result;
        }
    }

    /**
     * Subroutine of Simplify
     */
    private List<Location> simplifyAndConcatenate(List<Location> points1, List<Location> points2,
                                                  Distance tolerance)
    {
        List<Location> result1 = new ArrayList<>(simplify(points1, tolerance));
        List<Location> result2 = new ArrayList<>(simplify(points2, tolerance));
        // Add all from the second list except its first point to avoid duplicates
        result1.addAll(result2.subList(1, result2.size()));
        return result1;
    }
}
