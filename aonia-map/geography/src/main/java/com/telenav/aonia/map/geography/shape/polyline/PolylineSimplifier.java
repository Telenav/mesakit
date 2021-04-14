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

package com.telenav.aonia.map.geography.shape.polyline;

import com.telenav.aonia.map.geography.project.lexakai.diagrams.DiagramPolyline;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.shape.segment.Segment;
import com.telenav.aonia.map.measurements.geographic.Distance;

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
    public Polyline simplify(final Polyline polyline, final Distance tolerance)
    {
        final var simplified = simplify(new ArrayList<>(polyline.locations()), tolerance);
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
    private List<Location> simplify(final List<Location> points, final Distance tolerance)
    {
        // Terminal Case
        if (points.size() <= 2)
        {
            return points;
        }
        // Weird case, when we have a loop
        if (points.get(0).equals(points.get(points.size() - 1)))
        {
            final var size = points.size();
            final var points1 = points.subList(0, size / 2 + 1);
            final var points2 = points.subList(size / 2, size - 1);
            // split in 2 and simplify each side
            return simplifyAndConcatenate(points1, points2, tolerance);
        }
        // Non-Terminal Case
        final var size = points.size();
        final var segment = new Segment(points.get(0), points.get(size - 1));
        Distance maximumSnapDistance = null;
        var indexOfFarthestPoint = -1;
        final var snapper = new PolylineSnapper();
        // Find the farthest point
        for (var i = 1; i < size - 1; i++)
        {
            final var candidate = points.get(i);
            final var snapDistance = snapper.snap(segment, candidate).distanceToSource();
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
            final var points1 = points.subList(0, indexOfFarthestPoint + 1);
            final var points2 = points.subList(indexOfFarthestPoint, size);
            // Keep the point and recurse when applicable
            return simplifyAndConcatenate(points1, points2, tolerance);
        }
        else
        {
            // Remove all the points together and keep the Segment, because they must all be
            // within the tolerance
            final List<Location> result = new ArrayList<>();
            result.add(segment.start());
            result.add(segment.end());
            return result;
        }
    }

    /**
     * Subroutine of Simplify
     */
    private List<Location> simplifyAndConcatenate(final List<Location> points1, final List<Location> points2,
                                                  final Distance tolerance)
    {
        final List<Location> result1 = new ArrayList<>(simplify(points1, tolerance));
        final List<Location> result2 = new ArrayList<>(simplify(points2, tolerance));
        // Add all from the second list except its first point to avoid duplicates
        result1.addAll(result2.subList(1, result2.size()));
        return result1;
    }
}
