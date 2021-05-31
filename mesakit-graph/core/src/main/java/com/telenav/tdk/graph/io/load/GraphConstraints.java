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

package com.telenav.kivakit.graph.io.load;

import com.telenav.kivakit.collections.iteration.iterables.FilteredIterable;
import com.telenav.kivakit.kernel.interfaces.object.Matcher;
import com.telenav.kivakit.kernel.language.matching.All;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.string.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.messaging.messages.MessageList;
import com.telenav.kivakit.kernel.messaging.messages.status.Problem;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.collections.EdgeSequence;
import com.telenav.kivakit.graph.collections.RelationSequence;
import com.telenav.kivakit.graph.collections.VertexSequence;
import com.telenav.kivakit.map.geography.rectangle.Rectangle;

/**
 * A set of constraints for matching edges, vertexes and attributes in a graph. Can be used to limit what part of a
 * {@link Graph} is loaded via {@link Graph#load(GraphLoader, GraphConstraints)}.
 *
 * @author jonathanl (shibo)
 */
public class GraphConstraints
{
    /**
     * Constraints that match all graph data
     */
    public static final GraphConstraints ALL = new GraphConstraints();

    /** The constrained bounding area */
    private Rectangle bounds = Rectangle.MAXIMUM;

    /** An edge matcher for filtering */
    private Matcher<Edge> edgeMatcher = new All<>();

    /** A vertex matcher for filtering */
    private Matcher<Vertex> vertexMatcher = new All<>();

    /** A place matcher for filtering */
    private Matcher<Place> placeMatcher = new All<>();

    /** A relation matcher for filtering */
    private Matcher<EdgeRelation> relationMatcher = new All<>();

    /**
     * Construct default graph constraints, which includes all edge types, all vertex attributes and all edge attributes
     * for the entire globe.
     */
    public GraphConstraints()
    {
    }

    /**
     * Copy constructor
     *
     * @param that The object to copy
     */
    private GraphConstraints(final GraphConstraints that)
    {
        bounds = that.bounds;
        edgeMatcher = that.edgeMatcher;
        placeMatcher = that.placeMatcher;
        vertexMatcher = that.vertexMatcher;
        relationMatcher = that.relationMatcher;
    }

    /**
     * @return The constraint bounds
     */
    @KivaKitIncludeProperty
    public Rectangle bounds()
    {
        return bounds;
    }

    public boolean clips(final Edge edge)
    {
        return bounds().containment(edge.fromLocation()) != bounds().containment(edge.toLocation());
    }

    /**
     * @return A Matcher that satisfies all the constraints, including the specified matcher as well as the bounds.
     */
    public Matcher<Edge> edgeMatcher()
    {
        return this::includes;
    }

    /**
     * @return The sequence of edges matching this constraints object
     */
    public EdgeSequence edges(final Iterable<Edge> edges)
    {
        return new EdgeSequence(new FilteredIterable<>(edges, this::includes));
    }

    /**
     * @return True if the constraints include the given edge
     */
    public boolean includes(final Edge edge)
    {
        if (edge.isInside(bounds()))
        {
            return edgeMatcher.matches(edge);
        }
        return false;
    }

    public boolean includes(final EdgeRelation relation)
    {
        return relationMatcher.matches(relation);
    }

    public boolean includes(final Place place)
    {
        return placeMatcher.matches(place);
    }

    /**
     * @return True if the constraints include the given vertex
     */
    public boolean includes(final Vertex vertex)
    {
        return vertexMatcher.matches(vertex);
    }

    public Matcher<Place> placeMatcher()
    {
        return this::includes;
    }

    public RelationSequence relations(final Iterable<EdgeRelation> relations)
    {
        return new RelationSequence(new FilteredIterable<>(relations, this::includes));
    }

    /**
     * Checks to see if this set of graph constraints satisfies that set of constraints.
     *
     * @return A list of any {@link Problem}s where the required constraints are not satisfied by this constraints
     * object (note that this does not include filtering). All of the required attributes must be contained in this. In
     * addition the bounding rectangles of the two constraint objects must intersect (or there can be no matched graph
     * data).
     */
    public MessageList<Problem> satisfies(final GraphConstraints required)
    {
        final var messages = new MessageList<Problem>(new All<>());
        if (!required.bounds.intersects(bounds))
        {
            messages.add(new Problem("Bounds $ doesn't intersect with $", bounds, required.bounds));
        }
        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public Matcher<Vertex> vertexMatcher()
    {
        return this::includes;
    }

    public VertexSequence vertexes(final Iterable<Vertex> vertexes)
    {
        return new VertexSequence(new FilteredIterable<>(vertexes, this::includes));
    }

    /**
     * @return A copy of this constraints object with the given bounds
     */
    public GraphConstraints withBounds(final Rectangle bounds)
    {
        final var constraints = new GraphConstraints(this);
        constraints.bounds = bounds;
        return constraints;
    }

    /**
     * @return A copy of this constraints object with the given edge matcher
     */
    public GraphConstraints withEdgeMatcher(final Matcher<Edge> edgeMatcher)
    {
        final var constraints = new GraphConstraints(this);
        constraints.edgeMatcher = edgeMatcher;
        return constraints;
    }

    public GraphConstraints withEdgeRelationMatcher(final Matcher<EdgeRelation> matcher)
    {
        final var constraints = new GraphConstraints(this);
        constraints.relationMatcher = matcher;
        return constraints;
    }

    public GraphConstraints withPlaceMatcher(final Matcher<Place> matcher)
    {
        final var constraints = new GraphConstraints(this);
        constraints.placeMatcher = matcher;
        return constraints;
    }

    public GraphConstraints withVertexMatcher(final Matcher<Vertex> matcher)
    {
        final var constraints = new GraphConstraints(this);
        constraints.vertexMatcher = matcher;
        return constraints;
    }

    public GraphConstraints withoutEdgeRelations()
    {
        final var constraints = new GraphConstraints(this);
        constraints.relationMatcher = (relation) -> false;
        return constraints;
    }
}
