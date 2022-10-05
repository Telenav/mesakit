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

import com.telenav.kivakit.collections.set.operations.Intersection;
import com.telenav.kivakit.collections.set.operations.Subset;
import com.telenav.kivakit.collections.set.operations.Union;
import com.telenav.kivakit.collections.set.operations.Without;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.Streams;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.messaging.messages.status.Warning;
import com.telenav.kivakit.core.string.Join;
import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphLimits.Limit;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapRelationIdentifier;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.core.time.Frequency.EVERY_MINUTE;

/**
 * A set of relations. Supports {@link #union(RelationSet)} and {@link #without(Set)} operations, that logically combine
 * this set of relations with another set of relations without creating a physical set.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public class RelationSet implements Set<EdgeRelation>
{
    public static final RelationSet EMPTY = new RelationSet(Maximum._0, Estimate._0, Collections.emptySet());

    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * @return A relation set for a collection of relations
     */
    @SuppressWarnings({ "unchecked" })
    public static RelationSet forCollection(Maximum maximumSize,
                                            Collection<? extends EdgeRelation> collection)
    {
        if (collection instanceof Set)
        {
            return new RelationSet(maximumSize, Estimate.estimate(collection), (Set<EdgeRelation>) collection);
        }
        else
        {
            var set = new RelationSet(maximumSize, Estimate.estimate(collection));
            set.addAll(collection);
            return set;
        }
    }

    public static RelationSet forIdentifierArray(Graph graph, LongArray identifiers)
    {
        var relations = new RelationSet(Limit.RELATIONS, Estimate._16);
        var iterator = identifiers.iterator();
        while (iterator.hasNext())
        {
            var identifier = iterator.next();
            relations.add(graph.relationForIdentifier(new RelationIdentifier(identifier)));
        }
        return relations;
    }

    /**
     * @return A relation set for a sequence of relations
     */
    public static RelationSet forIterable(Maximum maximum, Iterable<? extends EdgeRelation> collection)
    {
        var set = new RelationSet(maximum, Estimate.estimate(collection));
        for (EdgeRelation relation : collection)
        {
            set.add(relation);
        }
        return set;
    }

    /**
     * @return A relation set containing a single relation
     */
    public static RelationSet singleton(EdgeRelation relation)
    {
        return new RelationSet(Maximum._1, Estimate._1, Collections.singleton(relation));
    }

    public static RelationSet threadSafe(Maximum maximumSize)
    {
        return new RelationSet(maximumSize, Estimate._1024, Collections.synchronizedSet(new HashSet<>()));
    }

    public static class Converter extends BaseStringConverter<RelationSet>
    {
        private final EdgeRelation.Converter converter;

        private final Separators separators;

        public Converter(Graph graph, Separators separators, Listener listener)
        {
            super(listener);
            this.separators = separators;
            converter = new EdgeRelation.Converter(graph, listener);
        }

        @Override
        protected String onToString(RelationSet value)
        {
            return value.joinedIdentifiers(separators.current());
        }

        @Override
        protected RelationSet onToValue(String value)
        {
            var relations = new RelationSet(Limit.RELATIONS, Estimate._16);
            if (!Strings.isEmpty(value))
            {
                for (var relation : value.split(separators.current()))
                {
                    relations.add(converter.convert(relation));
                }
            }
            return relations;
        }
    }

    /**
     * The underlying set of relations
     */
    private final Set<EdgeRelation> relations;

    /**
     * The maximum number of relations in this set
     */
    private final Maximum maximumSize;

    /**
     * Estimated number of relations in this set
     */
    private final Estimate initialSize;

    /**
     * Constructs a relation set with the given maximum size
     */
    public RelationSet(Estimate initialSize)
    {
        this(Maximum.MAXIMUM, initialSize, new HashSet<>());
    }

    /**
     * Constructs a relation set with the given maximum size
     */
    public RelationSet(Maximum maximumSize, Estimate initialSize)
    {
        this(maximumSize, initialSize, new HashSet<>());
    }

    /**
     * Constructs a relation set with an underlying set of relations
     */
    public RelationSet(Maximum maximumSize, Estimate initialSize, Set<EdgeRelation> relations)
    {
        this.maximumSize = maximumSize;
        this.initialSize = initialSize;
        this.relations = relations;
    }

    /**
     * Adds the given relation to this set
     */
    @Override
    public boolean add(EdgeRelation relation)
    {
        if (relations.size() == maximumSize.asInt())
        {
            LOGGER.transmit(new Warning("EdgeRelationSet maximum size of $ elements would be exceeded. Ignoring relation.",
                    maximumSize).maximumFrequency(EVERY_MINUTE));
            return false;
        }
        return relations.add(relation);
    }

    /**
     * Adds all relations in the given collection to this relation set
     */
    @Override
    public boolean addAll(Collection<? extends EdgeRelation> relations)
    {
        var changed = false;
        for (EdgeRelation relation : relations)
        {
            changed = add(relation) || changed;
        }
        return changed;
    }

    /**
     * Adds all relations in the given array to this relation set
     */
    public void addAll(EdgeRelation[] relations)
    {
        for (var relation : relations)
        {
            add(relation);
        }
    }

    /**
     * @return This set of {@link EdgeRelation}s as {@link HeavyWeightRelation}s
     */
    public RelationSet asHeavyWeight()
    {
        // Determine if all relations are already heavyweight
        var heavyweight = true;
        for (var relation : this)
        {
            if (!(relation instanceof HeavyWeightRelation))
            {
                heavyweight = false;
                break;
            }
        }

        // If all relations are heavyweight
        if (heavyweight)
        {
            // the set is already a set of heavyweight relations
            return this;
        }

        // Return a copy of this set as temporary relations
        var copy = new RelationSet(maximumSize(), initialSize());
        for (var relation : this)
        {
            copy.add(relation.asHeavyWeight());
        }
        return copy;
    }

    /**
     * @return This relation set as a primitive {@link LongArray} of relation identifiers
     */
    public LongArray asIdentifierArray()
    {
        var identifiers = new LongArray("temporary");
        for (var relation : relations)
        {
            identifiers.add(relation.identifierAsLong());
        }
        return identifiers;
    }

    /**
     * @return This relation set as a sequence
     */
    public RelationSequence asSequence()
    {
        return new RelationSequence(this);
    }

    /**
     * @return The smallest rectangle that contains all relations in this set
     */
    public Rectangle bounds()
    {
        return Rectangle.fromBoundedObjects(this);
    }

    /**
     * Clears this set
     */
    @Override
    public void clear()
    {
        relations.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object value)
    {
        if (value instanceof EdgeRelation)
        {
            return relations.contains(value);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> relations)
    {
        for (Object object : relations)
        {
            if (!contains(object))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return True if this set contains any relation in the given collection
     */
    public boolean containsAny(Collection<EdgeRelation> relations)
    {
        for (var relation : relations)
        {
            if (contains(relation))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The number of relations in this set
     */
    public Count count()
    {
        return Count.count(size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof RelationSet)
        {
            var that = (RelationSet) object;
            return relations.equals(that.relations);
        }
        return false;
    }

    /**
     * @return The first relation in this set (an arbitrary relation since sets are not ordered)
     */
    public EdgeRelation first()
    {
        if (!isEmpty())
        {
            return relations.iterator().next();
        }
        return null;
    }

    /**
     * Determines if this EdgeRelationSet has any matching relations
     */
    public boolean hasMatch(Matcher<EdgeRelation> matcher)
    {
        return !matching(matcher).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return relations.hashCode();
    }

    public Estimate initialSize()
    {
        return initialSize;
    }

    /**
     * @return The set of all relations in this set that are also in the given set
     */
    public RelationSet intersection(RelationSet that)
    {
        return new RelationSet(maximumSize(), initialSize(), new Intersection<>(this, that));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return relations.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<EdgeRelation> iterator()
    {
        return relations.iterator();
    }

    /**
     * @return All the relation identifiers in this relation set joined into a string using the given separator. The
     * order of identifiers is undefined.
     */
    public String joinedIdentifiers(String separator)
    {
        return Join.join(this, separator, value -> Long.toString(value.identifierAsLong()));
    }

    /**
     * @return The total length of all relations in this set
     */
    public Distance length()
    {
        var totalLengthInMillimeters = 0L;
        for (var relation : this)
        {
            totalLengthInMillimeters += relation.length().asMillimeters();
        }
        return Distance.millimeters(totalLengthInMillimeters);
    }

    /**
     * @return The set of all matching relations
     */
    public RelationSet matching(Matcher<EdgeRelation> matcher)
    {
        return new RelationSet(maximumSize(), initialSize(), new Subset<>(this, matcher));
    }

    /**
     * @return The maximum size of this relation set
     */
    public Maximum maximumSize()
    {
        return maximumSize;
    }

    /**
     * @return The most important relation in this set, as determined by
     * {@link EdgeRelation#isMoreImportantThan(EdgeRelation)}
     */
    public EdgeRelation mostImportant()
    {
        EdgeRelation important = null;
        for (var relation : this)
        {
            if (important == null || relation.isMoreImportantThan(important))
            {
                important = relation;
            }
        }
        return important;
    }

    public Set<MapRelationIdentifier> relationIdentifiers()
    {
        Set<MapRelationIdentifier> identifiers = new HashSet<>();
        for (var relation : this)
        {
            identifiers.add((MapRelationIdentifier) relation.mapIdentifier());
        }
        return identifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object object)
    {
        if (object instanceof EdgeRelation)
        {
            return relations.remove(object);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> relations)
    {
        for (Object relation : relations)
        {
            remove(relation);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean retainAll(Collection<?> relations)
    {
        return unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return relations.size();
    }

    @Override
    public Stream<EdgeRelation> stream()
    {
        return Streams.stream(this);
    }

    public Stream<EdgeRelation> stream(Streams.Processing processing)
    {
        return Streams.stream(processing, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray()
    {
        return unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T @NotNull [] a)
    {
        return unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[" + joinedIdentifiers(", ") + "]";
    }

    /**
     * @return The union of this relation set with another set of relations
     */
    public RelationSet union(RelationSet relations)
    {
        return new RelationSet(maximumSize(), initialSize(), new Union<>(this, relations));
    }

    /**
     * @return The set of relations within the given bounds
     */
    public RelationSet within(Rectangle bounds)
    {
        return matching(relation -> bounds.intersects(relation.bounds()));
    }

    /**
     * @return This set of relations without the given relation
     */
    public RelationSet without(EdgeRelation exclude)
    {
        return without(Collections.singleton(exclude));
    }

    /**
     * @return The set of all relations that don't match
     */
    public RelationSet without(Matcher<EdgeRelation> matcher)
    {
        return new RelationSet(maximumSize(), initialSize(),
                new Subset<>(this, relation -> !matcher.matches(relation)));
    }

    /**
     * @return This set of relations without the given set of relations
     */
    public RelationSet without(Set<EdgeRelation> exclude)
    {
        return new RelationSet(maximumSize(), initialSize(), new Without<>(this, exclude));
    }
}
