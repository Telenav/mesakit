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

package com.telenav.mesakit.graph.collections;

import com.telenav.kivakit.core.collections.iteration.BaseIterator;
import com.telenav.kivakit.core.language.iteration.Streams;

import com.telenav.kivakit.core.value.level.Percent;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.graph.navigation.RouteLimiter;
import com.telenav.mesakit.graph.navigation.limiters.LengthRouteLimiter;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;

import java.util.Iterator;
import java.util.stream.Stream;

import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import static com.telenav.mesakit.map.measurements.geographic.Angle._180_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle._45_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle._90_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle.degrees;

/**
 * A pair of edges.
 *
 * @author jonathanl (shibo)
 */
public class EdgePair implements Iterable<Edge>, Bounded
{
    public enum DoubleDigitizedType
    {
        MATCHING_NAMES,
        MISMATCHED_NAMES
    }

    /**
     * The first edge of the pair
     */
    private final Edge first;

    /**
     * The second edge of the pair
     */
    private final Edge second;

    /**
     * Construct a pair
     */
    public EdgePair(Edge first, Edge second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * @return The angle between the headings of the two edges in this pair using the given {@link Chirality}, whether
     * the edges are connected or not.
     */
    public Angle angleBetween(Chirality chirality)
    {
        return first.heading().difference(second.heading(), chirality);
    }

    /**
     * @return This pair as a connected route of two edges
     */
    public Route asRoute()
    {
        return Route.forEdges(this);
    }

    /**
     * @return The average heading of the two edges in this pair
     */
    public Heading averageHeading(Chirality chirality)
    {
        return first.heading().bisect(second.heading(), chirality);
    }

    /**
     * @return The smallest rectangle that contains both edges in this pair
     */
    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromBoundedObjects(this);
    }

    /**
     * @return The percentage in which the two edges are close to one another given distance and angle constraints
     */
    public Percent closeness(Distance maximumSeparation, Angle maximumHeadingDeviation)
    {
        var augmentationDistance = Distance.TEN_METERS;
        var first = this.first.roadShape().augmented(augmentationDistance);
        var second = this.second.roadShape().augmented(augmentationDistance);
        return first.closeness(second, maximumSeparation, maximumHeadingDeviation);
    }

    /**
     * @return The vertex connecting the two edges (or null if they are not connected)
     */
    public Vertex connectingVertex()
    {
        return first.vertexConnecting(second);
    }

    /**
     * @return True if this pair contains the given edge
     */
    public boolean contains(Edge edge)
    {
        return first.equals(edge) || second.equals(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof EdgePair)
        {
            var that = (EdgePair) other;
            return first().equals(that.first()) && second().equals(that.second());
        }
        return false;
    }

    /**
     * @return The first edge in this pair
     */
    public Edge first()
    {
        return first;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return first.hashCode() + second.hashCode();
    }

    /**
     * @return True if the two edges in this pair are connected
     */
    public boolean isConnected()
    {
        return first.isConnectedTo(second);
    }

    /**
     * @return True if this edge pair is connected within a given distance limit along some non-branching route
     */
    public boolean isConnectedByRoute(Navigator navigator, Distance within)
    {
        RouteLimiter limiter = new LengthRouteLimiter(within, LengthRouteLimiter.Type.STRICT);

        return isConnected()
                || first.outRoute(navigator, limiter).overlaps(second.outRoute(navigator, limiter));
    }

    /**
     * @return True if the two edges are connected in the same direction so you can go from the first edge to the second
     * edge or from the second edge to the first edge.
     */
    public boolean isConnectedInSameDirection()
    {
        return first.from().equals(second.to()) || first.to().equals(second.from());
    }

    /**
     * @return True if this pair of edges forms a double-digitized pair
     */
    public boolean isDoubleDigitized()
    {
        return isDoubleDigitized(DoubleDigitizedType.MATCHING_NAMES, degrees(35));
    }

    /**
     * @return True if this pair of edges forms a double-digitized pair
     */
    public boolean isDoubleDigitized(DoubleDigitizedType type, Angle maximumHeadingDeviation)
    {
        // If both edges could be double digitized
        if (first.osmCouldBeDoubleDigitized() && second.osmCouldBeDoubleDigitized())
        {
            // headed in opposite directions, roughly parallel, and no further apart than the
            // maximum double digitization separation
            if (first.heading().isOppositeDirection(second.heading(), maximumHeadingDeviation)
                    && isParallel(maximumHeadingDeviation)
                    && closeness(first.roadType().maximumDoubleDigitizationSeparation(), maximumHeadingDeviation)
                    .isGreaterThan(Percent.of(50)))
            {
                // must have the same base name (not including modifiers)
                var firstName = first.roadName();
                var secondName = second.roadName();
                if (firstName != null && secondName != null)
                {
                    var matches = firstName.extractNameOnly().equals(secondName.extractNameOnly());
                    if (type == DoubleDigitizedType.MATCHING_NAMES)
                    {
                        return matches;
                    }
                    if (type == DoubleDigitizedType.MISMATCHED_NAMES)
                    {
                        return !matches;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return True if the edges overlap, meaning that the perpendicular drawn from at least one segment of the road
     * shape for one edge intersects a segment from the road shape for the other edge.
     */
    public boolean isOverlapping()
    {
        var aSegment = first.asSegment();
        var bSegment = second.asSegment();
        return aSegment.intersects(bSegment.perpendicular(second.fromLocation(), Distance.miles(50)))
                || aSegment.intersects(bSegment.perpendicular(second.toLocation(), Distance.miles(50)))
                || bSegment.intersects(aSegment.perpendicular(first.fromLocation(), Distance.miles(50)))
                || bSegment.intersects(aSegment.perpendicular(first.toLocation(), Distance.miles(50)));
    }

    /**
     * @return True if the two edges in this pair are parallel
     */
    public boolean isParallel()
    {
        return isParallel(Edge.PARALLEL_TOLERANCE);
    }

    /**
     * @return True if the two edges in this pair are parallel
     */
    public boolean isParallel(Angle tolerance)
    {
        return smallestAngleBetween().isLessThan(tolerance);
    }

    /**
     * @return True if the two edges in this pair are perpendicular
     */
    public boolean isPerpendicular()
    {
        return isPerpendicular(Edge.PERPENDICULAR_TOLERANCE);
    }

    /**
     * @return True if the two edges in this pair are perpendicular
     */
    public boolean isPerpendicular(Angle tolerance)
    {
        return smallestAngleBetween().isClose(_90_DEGREES, tolerance);
    }

    /**
     * @return True if the edges in this pair are on the same logical road (not considering direction modifiers like N,
     * SW or Eastbound)
     */
    public boolean isSameRoad()
    {
        var aRoad = first.roadName();
        var bRoad = second.roadName();
        if (aRoad != null && bRoad != null)
        {
            return aRoad.extractNameOnly().equals(bRoad.extractNameOnly());
        }
        return false;
    }

    /**
     * @return True if the edge pair form an angle that is straight within a tolerance of 45 degrees
     */
    public boolean isStraight()
    {
        return isStraight(_45_DEGREES);
    }

    /**
     * @return True if the edge pair form an angle that is straight within the given amount of tolerance
     */
    public boolean isStraight(Angle tolerance)
    {
        return smallestAngleBetween().isLessThan(tolerance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Edge> iterator()
    {
        return new BaseIterator<>()
        {
            private int index;

            @Override
            protected Edge onNext()
            {
                switch (index++)
                {
                    case 0:
                        return first;

                    case 1:
                        return second;

                    default:
                        return null;
                }
            }
        };
    }

    public Stream<Edge> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    /**
     * @return The second edge in this pair
     */
    public Edge second()
    {
        return second;
    }

    /**
     * @return The smallest angle formed by the two given edges. This value can never be greater than 90 degrees (if it
     * were 91 degrees, the complementary angle of 89 degrees would be smaller)
     */
    public Angle smallestAngleBetween()
    {
        var difference = first.heading().difference(second.heading(), Chirality.SMALLEST);
        if (!difference.isAcute())
        {
            return _180_DEGREES.minus(difference);
        }
        return difference;
    }

    public Stream<Edge> stream()
    {
        return Streams.stream(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[EdgePair a = " + first + ", b = " + second + "]";
    }
}
