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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.context.CallStack;
import com.telenav.kivakit.core.messaging.messages.status.Glitch;
import com.telenav.kivakit.core.messaging.messages.status.Problem;
import com.telenav.kivakit.core.messaging.messages.status.Quibble;
import com.telenav.kivakit.core.messaging.messages.status.Warning;
import com.telenav.kivakit.core.string.AsIndentedString;
import com.telenav.kivakit.core.string.CaseFormat;
import com.telenav.kivakit.core.string.ObjectIndenter;
import com.telenav.kivakit.core.string.StringConversions;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.interfaces.collection.Indexed;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.value.LongValued;
import com.telenav.kivakit.primitive.collections.LongKeyed;
import com.telenav.kivakit.validation.BaseValidator;
import com.telenav.kivakit.validation.Validatable;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.GraphElementIdentifier;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.mesakit.graph.specifications.common.place.HeavyWeightPlace;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.messaging.context.CallStack.Matching.SUBCLASS;
import static com.telenav.kivakit.core.messaging.context.CallStack.Proximity.DISTANT;
import static com.telenav.kivakit.interfaces.string.StringFormattable.Format.PROGRAMMATIC;

/**
 * Base class for {@link Edge}, {@link EdgeRelation}, {@link Vertex}, {@link ShapePoint} and {@link Place} elements in a
 * {@link Graph}. This abstraction allows them to be treated the same when they are, for example, stored in a spatial
 * index. It also allows for a common data indexing scheme and storage of common attributes.
 * <p>
 * Graph elements have a {@link Graph} and a {@link GraphElementIdentifier} subclass that can be retrieved with
 * {@link #identifier()}. An {@link Edge} has an {@link EdgeIdentifier}, a vertex has a {@link VertexIdentifier} and so
 * on. This identifier can also be retrieved as a long value when the situation warrants it. Graph elements also have a
 * {@link MapIdentifier} subclass retrieved by {@link #mapIdentifier()} that relates to the entity they were derived
 * from in the source data. For example, {@link Edge}s are associated with {@link PbfWayIdentifier}s, {@link Vertex}es
 * are associated with {@link PbfNodeIdentifier}s and so on.
 * <p>
 * <b>Identifiers</b>
 * <ul>
 *     <li>{@link #identifier()}</li>
 *     <li>{@link #identifierAsLong()}</li>
 *     <li>{@link #mapIdentifier()}</li>
 * </ul>
 * <p>
 * A graph element conforms to the owning graph's {@link DataSpecification} and has various {@link Attribute}s. The
 * attributes supported by a graph element can be retrieved with {@link #attributes()}, and whether an element supports
 * a particular attribute can be determined with {@link #supports(Attribute)}.
 * <p>
 * <b>Attributes</b>
 * <ul>
 *     <li>{@link #attributes()}</li>
 *     <li>{@link #supports(Attribute)}</li>
 * </ul>
 * <p>
 * Every element in a graph supports tagging with key / value pairs. These tags can be retrieved with:
 * <p>
 * <b>Tags</b>
 * <ul>
 *     <li>{@link #tag(String)}</li>
 *     <li>{@link #tagList()}</li>
 *     <li>{@link #tagMap()}</li>
 *     <li>{@link #tagValue(String)}</li>
 *     <li>{@link #hasTag(String)}</li>
 * </ul>
 * <p>
 * Graph elements have a {@link #bounds()} and the method {@link #isInside(Rectangle)}, determines if the element is
 * fully contained in the given rectangle.
 * <p>
 * Elements also have a variety of metadata attributes, which are common to all {@link DataSpecification}s.:
 * <p>
 * <b>Metadata</b>
 * <ul>
 *     <li>{@link #metadata()}</li>
 *     <li>{@link #dataSpecification()}</li>
 *     <li>{@link #lastModificationTime()}</li>
 *     <li>{@link #pbfChangeSetIdentifier()}</li>
 *     <li>{@link #pbfRevisionNumber()}</li>
 *     <li>{@link #pbfUserIdentifier()}</li>
 *     <li>{@link #pbfUserName()}</li>
 * </ul>
 * <p>
 * Graph elements are tiny objects, and mostly have only three fields:
 * <ul>
 *     <li><b>1.</b> <i>graph</i> - A reference to the {@link Graph} that contains the element</li>
 *     <li><b>2.</b> <i>identifier</i> - A {@link GraphElementIdentifier} used to identify the element in the graph</li>
 *     <li><b>3.</b> <i>index</i> - An index used to access element attributes the {@link GraphElementStore} for the type of element</li>
 * </ul>
 * The small size of graph elements (and most subclasses do not add any fields) makes it very fast to allocate new
 * elements (particularly with modern generational garbage collectors). This design pattern is known as "flyweight",
 * which gives the name "heavyweight" to graph elements that don't make use of this pattern but instead store all
 * their attributes in fields ({@link HeavyWeightEdge}, {@link HeavyWeightVertex}, {@link HeavyWeightRelation}
 * and {@link HeavyWeightPlace}). Heavyweight graph elements are inefficient to construct relative to flyweight elements
 * and are used only temporarily during graph loading as well as in testing.
 * <p>
 * Graph elements support the {@link #hashCode()} / {@link #equals(Object)} contract and are {@link Validatable}.
 * Subclasses override the {@link Validatable#validator(ValidationType)} method to provide validation before elements
 * are saved to a {@link GraphArchive} and before a {@link GraphLoader} adds them to a {@link GraphElementStore}.
 * To allow elements to be treated the same as many other objects that are {@link LongValued} to a long value (in
 * this case the identifier), {@link GraphElement} implements {@link #longValue()}.
 *
 * @author jonathanl (shibo)
 * @see GraphElementIdentifier
 * @see MapIdentifier
 * @see DataSpecification
 * @see Attribute
 * @see Tag
 * @see PbfTagMap
 * @see PbfTagList
 * @see GraphStore
 * @see GraphElementStore
 * @see DataSpecification
 * @see Validatable
 * @see LongValued
 * @see Iterable
 * @see Indexed
 * @see GraphLoader
 * @see AsIndentedString
 */
@SuppressWarnings("unused")
public abstract class GraphElement implements
        Named,
        Indexed,
        LongValued,
        LongKeyed,
        Validatable,
        AsIndentedString
{
    /** Validation when adding elements */
    public static final ValidationType VALIDATE_RAW = new ValidationType() {};

    /** Null index */
    public static final int NULL_INDEX = 0;

    /** Null identifier */
    public static final int NULL_IDENTIFIER = 0;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    protected abstract class ElementValidator extends BaseValidator
    {
        @Override
        protected Glitch glitch(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.glitch(name() + " identifier " + identifier + " is invalid because " + message, parameters);
            }
            else
            {
                return addGlitch(message, parameters);
            }
        }

        @Override
        protected Problem problem(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.problem(name() + " identifier " + identifier + " is invalid because " + message, parameters);
            }
            else
            {
                return addProblem(message, parameters);
            }
        }

        @Override
        protected Quibble quibble(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.quibble(name() + " identifier " + identifier + " is invalid because " + message, parameters);
            }
            else
            {
                return addQuibble(message, parameters);
            }
        }

        @Override
        protected Warning warning(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.warning(name() + " identifier " + identifier + " is invalid because " + message, parameters);
            }
            else
            {
                return addWarning(message, parameters);
            }
        }
    }

    /** The graph that this element belongs to */
    private transient Graph graph;

    /** The identifier for this element */
    private long identifier = NULL_IDENTIFIER;

    /** The index of this element, used to find data quickly in {@link GraphElement} stores */
    private int index = NULL_INDEX;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     * <p>
     * If debug is enabled for this class, a stack check is executed to determine if this element was constructed from a
     * {@link DataSpecification}, which ensures that it is properly constructed and  initialized. This check is disabled
     * by default because it is quite expensive to check the call stack.
     */
    protected GraphElement()
    {
        // If we're having a bad day,
        if (DEBUG.isDebugOn())
        {
            // check to see if we were called from a data specification at some point. No graph
            // element should ever be constructed except from a data specification.
            ensure(CallStack.callerOf(DISTANT, SUBCLASS, DataSpecification.class) != null,
                    "$ was not constructed from a data specification", getClass().getSimpleName());
        }
    }

    /**
     * Returns a copy of this element as a heavyweight
     */
    public abstract GraphElement asHeavyWeight();

    @Override
    public String asString(@NotNull Format format)
    {
        if (format == PROGRAMMATIC)
        {
            return Long.toString(identifierAsLong());
        }

        var indenter = new ObjectIndenter(format)
                .levels(Maximum._8)
                .pruneAt(Edge.class);

        var string = asString(format, indenter).toString();
        return format == Format.HTML ? StringConversions.toHtmlString(string) : string;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectIndenter asString(Format format, ObjectIndenter indenter)
    {
        if (indenter.indentationLevel() > 1 && !indenter.canExplore(this))
        {
            indenter.add(name());
        }
        else
        {
            // convert to element type and add section to builder,
            indenter.indented(name(), () ->
            {
                // and add the elements properties
                for (var property : properties())
                {
                    var isNone = property.attribute().equals(GraphElementAttributes.get().NONE);
                    if (isNone || dataSpecification().supports(property.attribute()))
                    {
                        var propertyName = property.name() + (isNone ? "*" : "");
                        var value = property.valueFromObject(this);
                        if (value != null && indenter.canExplore(value))
                        {
                            if (value instanceof Iterable<?> collection)
                            {
                                indenter.label(CaseFormat.camelCaseToHyphenated(propertyName));
                                indenter.bracketed(collection, indenter::asString);
                            }
                            else if (value instanceof AsIndentedString)
                            {
                                indenter.indented(propertyName, () -> ((AsIndentedString) value).asString(format, indenter));
                            }
                            else
                            {
                                indenter.labeled(propertyName, value);
                            }
                        }
                        else
                        {
                            indenter.labeled(propertyName, value);
                        }
                    }
                }
            });
        }
        return indenter;
    }

    /**
     * Returns the attributes for this element
     */
    public abstract GraphElementAttributes<?> attributes();

    /**
     * Returns a bounding rectangle that completely contains this element
     */
    public abstract Rectangle bounds();

    /**
     * Convenience method to get the data specification from the graph that owns this element
     *
     * @return The data specification for this element
     */
    public DataSpecification dataSpecification()
    {
        return graph().dataSpecification();
    }

    /**
     * Convenience method to get the data supplier from the graph that owns this element
     *
     * @return The data supplier for this element
     */
    public DataSupplier dataSupplier()
    {
        return metadata().dataSupplier();
    }

    /**
     * Returns true if the given object is a {@link GraphElement} with the same identifier as this element
     */
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof GraphElement that)
        {
            return identifierAsLong() == that.identifierAsLong();
        }
        return false;
    }

    /**
     * Returns the graph that owns this element
     */
    public Graph graph()
    {
        return graph;
    }

    /**
     * Sets ownership of this element
     *
     * @param graph The graph that owns this element
     */
    public void graph(Graph graph)
    {
        this.graph = graph;
    }

    /**
     * Returns true if this element has a tag with the given key
     */
    public final boolean hasTag(String key)
    {
        return tag(key) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Long.hashCode(identifier);
    }

    /**
     * Returns the identifier for this graph element
     */
    public abstract GraphElementIdentifier identifier();

    /**
     * Assigns this element a graph element identifier
     */
    public final void identifier(long identifier)
    {
        this.identifier = identifier;
    }

    /**
     * Returns the identifier for this element
     */
    public long identifierAsLong()
    {
        if (identifier == NULL_IDENTIFIER)
        {
            identifier = store().retrieveIdentifier(index);
        }
        return identifier;
    }

    /**
     * Returns the index for this element, suitable for quickly retrieving data from this element's corresponding
     * {@link GraphElementStore}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public int index()
    {
        if (index == NULL_INDEX)
        {
            index = store().retrieveIndex(this);
            assert index > 0;
        }
        return index;
    }

    /**
     * Sets the index for this graph element
     */
    public void index(int index)
    {
        assert index > 0;
        this.index = index;
    }

    /** @return True if this element is heavyweight, false if it is flyweight. */
    public boolean isHeavyWeight()
    {
        return false;
    }

    /**
     * Returns true if this element is completely contained within the given bounding rectangle
     */
    public abstract boolean isInside(Rectangle bounds);

    /**
     * Convenience method
     *
     * @return True if this element belongs to an OSM graph
     */
    public boolean isOsm()
    {
        return dataSpecification().isOsm();
    }

    /**
     * Convenience method
     *
     * @return True if this element belongs to a UniDb graph
     */
    public boolean isUniDb()
    {
        return dataSpecification().isUniDb();
    }

    /**
     * Returns the key to use for this graph element in maps
     */
    @Override
    public long key()
    {
        return identifierAsLong();
    }

    /**
     * Returns the last modification time of this graph element
     */
    public Time lastModificationTime()
    {
        return store().retrieveLastModificationTime(this);
    }

    @Override
    public long longValue()
    {
        return index();
    }

    /**
     * Returns the map identifier (node, way or relation) of this element
     */
    public abstract MapIdentifier mapIdentifier();

    /**
     * Convenience method to get {@link Metadata} from the graph that owns this element
     *
     * @return The metadata for this element
     */
    public Metadata metadata()
    {
        return graph().metadata();
    }

    @Override
    public String name()
    {
        return CaseFormat.hyphenatedName(getClass()) + " " + identifier();
    }

    /**
     * Returns the PBF-specific identifier for the most recent set of changes where this element was altered
     */
    public PbfChangeSetIdentifier pbfChangeSetIdentifier()
    {
        return store().retrievePbfChangeSetIdentifier(this);
    }

    /**
     * Returns the current PBF-specific revision number of this element
     */
    public PbfRevisionNumber pbfRevisionNumber()
    {
        return store().retrievePbfRevisionNumber(this);
    }

    /**
     * Returns the user identifier for the user that last changed this element
     */
    public PbfUserIdentifier pbfUserIdentifier()
    {
        return store().retrievePbfUserIdentifier(this);
    }

    /**
     * Returns the username of the user that last changed this element
     */
    public PbfUserName pbfUserName()
    {
        return store().retrievePbfUserName(this);
    }

    /**
     * Returns propertyMap for this graph element
     */
    public abstract GraphElementPropertySet<? extends GraphElement> properties();

    /**
     * Returns true if this element supports the given attribute, under the owning graph's data specification
     */
    public final boolean supports(Attribute<?> attribute)
    {
        return dataSpecification().supports(attribute);
    }

    /**
     * Returns a tag for the given key, if any
     */
    public final Tag tag(String key)
    {
        return tagList().get(key);
    }

    /**
     * Returns a list of the tags for this element
     */
    public PbfTagList tagList()
    {
        if (supports(GraphElementAttributes.get().TAGS))
        {
            return store().retrieveTagList(this);
        }
        return PbfTagList.EMPTY;
    }

    /**
     * Returns a list of the tags for this element
     */
    public PbfTagMap tagMap()
    {
        if (supports(GraphElementAttributes.get().TAGS))
        {
            return store().retrieveTagMap(this);
        }
        return PbfTagMap.EMPTY;
    }

    /**
     * Returns the value of any tag with the given key, or null if no such tag exists
     */
    public final String tagValue(String key)
    {
        var tag = tag(key);
        if (tag != null)
        {
            return tag.getValue();
        }
        return null;
    }

    @Override
    public Validator validator(ValidationType type)
    {
        return new ElementValidator()
        {
            @Override
            protected void onValidate()
            {
                if (type != VALIDATE_RAW)
                {
                    problemIf(identifierAsLong() <= 0, "identifier $ is invalid", identifier());
                    problemIf(index() <= 0, "index $ is invalid", index());
                }
            }
        };
    }

    /**
     * Returns the store for this kind of element
     */
    @SuppressWarnings("rawtypes")
    protected abstract GraphElementStore store();

    /**
     * Returns the graph where this {@link GraphElement} is stored, distinct from {@link #graph()}. If this
     * {@link GraphElement} is in a normal graph, this value  will be the same as graph(). However, if this element is
     * in a world graph, this method will return the graph for the cell in which this resides (the cell graph) and
     * graph() will return the WorldGraph, as expected.
     */
    protected Graph subgraph()
    {
        return graph();
    }
}
