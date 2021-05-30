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

package com.telenav.tdk.graph.collections;

import com.telenav.tdk.core.kernel.language.iteration.BaseIterator;
import com.telenav.tdk.core.kernel.language.iteration.Streams;
import com.telenav.tdk.core.kernel.scalars.levels.Percentage;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.Vertex;
import com.telenav.tdk.graph.navigation.Navigator;
import com.telenav.tdk.graph.navigation.RouteLimiter;
import com.telenav.tdk.graph.navigation.limiters.LengthRouteLimiter;
import com.telenav.tdk.map.geography.rectangle.Bounded;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Angle;
import com.telenav.tdk.map.measurements.Angle.Chirality;
import com.telenav.tdk.map.measurements.Distance;
import com.telenav.tdk.map.measurements.Heading;

import java.util.Iterator;
import java.util.stream.Stream;

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
    public EdgePair(final Edge first, final Edge second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * @return The angle between the headings of the two edges in this pair using the given {@link Chirality}, whether
     * the edges are connected or not.
     */
    public Angle angleBetween(final Chirality chirality)
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
    public Heading averageHeading(final Chirality chirality)
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
    public Percentage closeness(final Distance maximumSeparation, final Angle maximumHeadingDeviation)
    {
        final var augmentationDistance = Distance.TEN_METERS;
        final var first = this.first.roadShape().augmented(augmentationDistance);
        final var second = this.second.roadShape().augmented(augmentationDistance);
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
    public boolean contains(final Edge edge)
    {
        return first.equals(edge) || second.equals(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof EdgePair)
        {
            final var that = (EdgePair) other;
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
    public boolean isConnectedByRoute(final Navigator navigator, final Distance within)
    {
        final RouteLimiter limiter = new LengthRouteLimiter(within, LengthRouteLimiter.Type.STRICT);

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
        return isDoubleDigitized(DoubleDigitizedType.MATCHING_NAMES, Angle.degrees(35));
    }

    /**
     * @return True if this pair of edges forms a double-digitized pair
     */
    public boolean isDoubleDigitized(final DoubleDigitizedType type, final Angle maximumHeadingDeviation)
    {
        // If both edges could be double digitized
        if (first.osmCouldBeDoubleDigitized() && second.osmCouldBeDoubleDigitized())
        {
            // headed in opposite directions, roughly parallel, and no further apart than the
            // maximum double digitization separation
            if (first.heading().isOppositeDirection(second.heading(), maximumHeadingDeviation)
                    && isParallel(maximumHeadingDeviation)
                    && closeness(first.roadType().maximumDoubleDigitizationSeparation(), maximumHeadingDeviation)
                    .isGreaterThan(new Percentage(50)))
            {
                // must have the same base name (not including modifiers)
                final var firstName = first.roadName();
                final var secondName = second.roadName();
                if (firstName != null && secondName != null)
                {
                    final var matches = firstName.extractNameOnly().equals(secondName.extractNameOnly());
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
        final var aSegment = first.asSegment();
        final var bSegment = second.asSegment();
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
    public boolean isParallel(final Angle tolerance)
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
    public boolean isPerpendicular(final Angle tolerance)
    {
        return smallestAngleBetween().isClose(Angle._90_DEGREES, tolerance);
    }

    /**
     * @return True if the edges in this pair are on the same logical road (not considering direction modifiers like N,
     * SW or Eastbound)
     */
    public boolean isSameRoad()
    {
        final var aRoad = first.roadName();
        final var bRoad = second.roadName();
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
        return isStraight(Angle._45_DEGREES);
    }

    /**
     * @return True if the edge pair form an angle that is straight within the given amount of tolerance
     */
    public boolean isStraight(final Angle tolerance)
    {
        return smallestAngleBetween().isLessThan(tolerance);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
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
        final var difference = first.heading().difference(second.heading(), Chirality.SMALLEST);
        if (!difference.isAcute())
        {
            return Angle._180_DEGREES.subtract(difference);
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
