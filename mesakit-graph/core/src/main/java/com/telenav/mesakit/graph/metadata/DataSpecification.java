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

package com.telenav.mesakit.graph.metadata;

import com.telenav.kivakit.core.collections.map.MultiMap;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.Differences;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.primitive.collections.array.scalars.IntArray;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.ShapePoint;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.io.convert.GraphConverter;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.common.CommonGraph;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeDifferences;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeProperties;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.place.HeavyWeightPlace;
import com.telenav.mesakit.graph.specifications.common.place.PlaceAttributes;
import com.telenav.mesakit.graph.specifications.common.place.PlaceDifferences;
import com.telenav.mesakit.graph.specifications.common.place.PlaceProperties;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.graph.specifications.common.relation.RelationAttributes;
import com.telenav.mesakit.graph.specifications.common.relation.RelationProperties;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.shapepoint.ShapePointProperties;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.mesakit.graph.specifications.common.vertex.VertexAttributes;
import com.telenav.mesakit.graph.specifications.common.vertex.VertexProperties;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeList;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeStore;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.map.data.formats.library.DataFormat;

import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.fail;
import static com.telenav.kivakit.core.ensure.Ensure.illegalArgument;
import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * A specification of {@link Graph} data and its storage, independent of format or supplier. For example, data of UniDB
 * origin can come from HERE or TomTom suppliers and it could be in PBF or CSV format (or it could have no format,
 * coming directly from UniDB by query). See {@link DataFormat}, {@link DataSupplier} and {@link Metadata} for details.
 * <p>
 * The set of {@link Attribute}s that are supported by a data specification can be queried with {@link
 * #supports(Attribute)} and {@link #attributes()}. The attributes supported by individual classes of {@link
 * GraphElement}s can be discovered with {@link #edgeAttributes()}, {@link #vertexAttributes()}, {@link
 * #relationAttributes()} and {@link #placeAttributes()}. Note that all of these methods have convenience methods in
 * {@link Graph}, so there's no need to directly query the graph's {@link DataSpecification}.
 * <p>
 * The factory methods of a data specification are used to create graphs and graph elements, as specified by a subclass.
 * enabling specialization.  <i>It is not allowable to create {@link Graph} objects directly.</i>. This restriction is
 * is enforced by a stack check in the {@link Graph} constructor, ensuring that all graphs are created and initialized
 * according to a data specification.
 * <p>
 * The abstract {@link CommonDataSpecification} specifies default graph, graph store and graph element implementations
 * which implement attributes that are common to both OSM and UniDb. Additional attributes and functionality are added
 * by the {@link OsmDataSpecification}.
 *
 * @author jonathanl (shibo)
 * @see CommonDataSpecification
 * @see OsmDataSpecification
 * @see Metadata
 * @see DataSupplier
 * @see DataFormat
 */
public abstract class DataSpecification implements NamedObject
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public static SwitchParser.Builder<DataSpecification> dataSpecificationSwitchParser(String name,
                                                                                        String description)
    {
        return SwitchParser.builder(DataSpecification.class).name(name).converter(new Converter(LOGGER))
                .description(description);
    }

    public static SwitchParser.Builder<DataSpecification> dataSpecificationSwitchParser()
    {
        return SwitchParser.builder(DataSpecification.class).name("data-specification").converter(new Converter(LOGGER))
                .description("The data specification to use, either OSM or UniDb");
    }

    /**
     * @param name The data specification name like "OSM" or "UniDb"
     * @return The data specification
     */
    public static DataSpecification parse(String name)
    {
        try
        {
            switch (name.toLowerCase())
            {
                case "osm":
                    name = "Osm";
                    break;

                case "unidb":
                    name = "UniDb";
                    break;

                default:
                    fail("Unrecognized data specification '$'", name);
            }

            var className = "com.telenav.mesakit.graph.specifications." + name.toLowerCase() + "." + name + "DataSpecification";
            var type = Class.forName(className);
            var getter = type.getMethod("get");
            return (DataSpecification) getter.invoke(null);
        }
        catch (Exception e)
        {
            illegalArgument("Unable to find DataSpecification class '$'", name);
        }
        return null;
    }

    /**
     * The type of data specification
     */
    public enum Type implements Matcher<Type>
    {
        /** Data common to all specifications */
        Common,

        /**
         * OSM data as specified by the OpenStreetMap community, not to be confused with the OSM PBF data format
         * (defined by the osmosis library), which can contain any kind of map data, including UniDb data.
         */
        OSM,

        /** Internal Telenav data from various sources, as specified by the UniDb specification */
        UniDb;

        @Override
        public boolean matches(Type type)
        {
            return this == type;
        }
    }

    public interface GraphElementFactory<T extends GraphElement>
    {
        T newElement(Graph graph, long identifier);
    }

    public static class Converter extends BaseStringConverter<DataSpecification>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected DataSpecification onToValue(String value)
        {
            return parse(value);
        }
    }

    /** Attributes in this specification by attribute store */
    private final MultiMap<Class<? extends AttributeStore>, Attribute<?>> storeAttributes = new MultiMap<>();

    /**
     * PrimitiveArray of integers indexed by attribute identifier. If the integer is zero, the attribute is not
     * supported. If it is non-zero, the attribute is supported
     */
    private IntArray supportedAttributes;

    /** List of attributes explicitly removed from this specification */
    private final Set<Attribute<?>> excludedAttributes = new HashSet<>();

    protected DataSpecification()
    {
        supportedAttributes = new IntArray(objectName() + ".supportedAttributes");
        supportedAttributes.initialize();
    }

    /**
     * @return All attributes supported by this specification
     */
    public AttributeList attributes()
    {
        var attributes = new AttributeList();
        for (var store : storeAttributes.keySet())
        {
            attributes.addAll(attributes(store));
        }
        return attributes;
    }

    /**
     * @return All attributes supported by this specification
     */
    public AttributeList attributes(Class<? extends AttributeStore> store)
    {
        var attributes = storeAttributes.get(store);
        return attributes == null ? new AttributeList() : new AttributeList(attributes);
    }

    public Differences compare(Edge a, Edge b)
    {
        return new EdgeDifferences(a, b);
    }

    public Differences compare(Place a, Place b)
    {
        return new PlaceDifferences(a, b);
    }

    public EdgeAttributes edgeAttributes()
    {
        return EdgeAttributes.get();
    }

    public EdgeProperties edgeProperties()
    {
        return EdgeProperties.get();
    }

    /**
     * Removes the given attribute to this data specification as supported by it
     */
    public void excludeAttribute(Attribute<?> attribute)
    {
        supportedAttributes.set(attribute.identifier(), 0);
        excludedAttributes.add(attribute);
        DEBUG.trace("Excluding attribute ${class}.$", getClass(), attribute);
    }

    public boolean isOsm()
    {
        return type() == Type.OSM;
    }

    public boolean isUniDb()
    {
        return type() == Type.UniDb;
    }

    public final Edge newEdge(Graph graph, long identifier)
    {
        return onNewEdge(graph, identifier);
    }

    public final Edge newEdge(Graph graph, long identifier, int index)
    {
        return onNewEdge(graph, identifier, index);
    }

    public EdgeStore newEdgeStore(Graph graph)
    {
        return unsupported();
    }

    public final Graph newGraph(Metadata metadata)
    {
        metadata.assertValid(Metadata.VALIDATE_EXCEPT_STATISTICS);
        return onNewGraph(metadata);
    }

    /**
     * @param metadata Metadata describing the data to convert
     * @return A graph converter suitable for converting the data described by metadata to a {@link Graph}
     */
    public GraphConverter newGraphConverter(Metadata metadata)
    {
        return onNewGraphConverter(metadata);
    }

    /**
     * @param metadata Metadata describing the data to load
     * @return A graph loader that can load data as described by the given metadata
     */
    public GraphLoader newGraphLoader(Metadata metadata)
    {
        return onNewGraphLoader(metadata);
    }

    public abstract GraphStore newGraphStore(Graph graph);

    public final HeavyWeightEdge newHeavyWeightEdge(Graph graph, long identifier)
    {
        return onNewHeavyWeightEdge(graph, identifier);
    }

    public final HeavyWeightPlace newHeavyWeightPlace(Graph graph, long identifier)
    {
        return onNewHeavyWeightPlace(graph, identifier);
    }

    public final HeavyWeightRelation newHeavyWeightRelation(Graph graph, long identifier)
    {
        return onNewHeavyWeightRelation(graph, identifier);
    }

    public final HeavyWeightVertex newHeavyWeightVertex(Graph graph, long identifier)
    {
        return onNewHeavyWeightVertex(graph, identifier);
    }

    public final Place newPlace(Graph graph, long identifier)
    {
        return onNewPlace(graph, identifier);
    }

    public abstract PlaceStore newPlaceStore(Graph graph);

    public final EdgeRelation newRelation(Graph graph, long identifier)
    {
        return onNewRelation(graph, identifier);
    }

    public abstract RelationStore newRelationStore(Graph graph);

    public final ShapePoint newShapePoint(Graph graph, long identifier)
    {
        return onNewShapePoint(graph, (int) identifier);
    }

    public abstract ShapePointStore newShapePointStore(Graph graph);

    public final Vertex newVertex(Graph graph, long identifier)
    {
        return onNewVertex(graph, identifier);
    }

    public abstract VertexStore newVertexStore(Graph graph);

    public boolean owns(Class<? extends AttributeStore> owner, Attribute<?> attribute)
    {
        return storeAttributes.get(owner).contains(attribute);
    }

    public PlaceAttributes placeAttributes()
    {
        return PlaceAttributes.get();
    }

    public PlaceProperties placeProperties()
    {
        return PlaceProperties.get();
    }

    public RelationAttributes relationAttributes()
    {
        return RelationAttributes.get();
    }

    public RelationProperties relationProperties()
    {
        return RelationProperties.get();
    }

    public GraphElementPropertySet<ShapePoint> shapePointProperties()
    {
        return ShapePointProperties.get();
    }

    public void supportedAttributes(IntArray supportedAttributes)
    {
        this.supportedAttributes = supportedAttributes;
    }

    public IntArray supportedAttributes()
    {
        return supportedAttributes;
    }

    /**
     * @return True if this data specification supports the given attribute
     */
    public boolean supports(Attribute<?> attribute)
    {
        return supportedAttributes.safeGet(attribute.identifier()) != 0;
    }

    @Override
    public final String toString()
    {
        return type().toString();
    }

    /**
     * @return Which data specification is being implemented
     */
    public abstract Type type();

    public VertexAttributes vertexAttributes()
    {
        return VertexAttributes.get();
    }

    public VertexProperties vertexProperties()
    {
        return VertexProperties.get();
    }

    /**
     * Adds the given attribute of the given store to this specification
     */
    protected void includeAttribute(Class<? extends AttributeStore> owner, Attribute<?> attribute)
    {
        var attributes = attributes(owner);
        if (!excludedAttributes.contains(attribute) && !attributes.contains(attribute))
        {
            storeAttributes.add(owner, attribute);
            supportedAttributes.set(attribute.identifier(), 1);
            DEBUG.trace("Including attribute ${class}.$", getClass(), attribute);
        }
    }

    /**
     * Adds the given attributes of the given store to this specification
     */
    protected void includeAttributes(Class<? extends AttributeStore> owner, AttributeList attributes)
    {
        for (var attribute : attributes)
        {
            includeAttribute(owner, attribute);
        }
    }

    protected Edge onNewEdge(Graph graph, long identifier)
    {
        return unsupported();
    }

    protected Edge onNewEdge(Graph graph, long identifier, int index)
    {
        return unsupported();
    }

    protected Graph onNewGraph(Metadata metadata)
    {
        metadata.assertValid(ValidationType.validateAll());
        return new CommonGraph(metadata);
    }

    protected GraphConverter onNewGraphConverter(Metadata metadata)
    {
        return unsupported();
    }

    protected GraphLoader onNewGraphLoader(Metadata metadata)
    {
        return unsupported();
    }

    protected HeavyWeightEdge onNewHeavyWeightEdge(Graph graph, long identifier)
    {
        return new HeavyWeightEdge(graph, identifier);
    }

    protected HeavyWeightPlace onNewHeavyWeightPlace(Graph graph, long identifier)
    {
        return new HeavyWeightPlace(graph, identifier);
    }

    protected HeavyWeightRelation onNewHeavyWeightRelation(Graph graph, long identifier)
    {
        return new HeavyWeightRelation(graph, identifier);
    }

    protected HeavyWeightVertex onNewHeavyWeightVertex(Graph graph, long identifier)
    {
        return new HeavyWeightVertex(graph, identifier);
    }

    protected Place onNewPlace(Graph graph, long identifier)
    {
        return new Place(graph, identifier);
    }

    protected EdgeRelation onNewRelation(Graph graph, long identifier)
    {
        return new EdgeRelation(graph, identifier);
    }

    protected ShapePoint onNewShapePoint(Graph graph, long identifier)
    {
        return new ShapePoint(graph, identifier);
    }

    protected Vertex onNewVertex(Graph graph, long identifier)
    {
        return new Vertex(graph, identifier);
    }

    protected void showAttributes()
    {
        for (var store : storeAttributes.keySet())
        {
            DEBUG.trace("Attributes supported by ${class}:\n    $",
                    store, storeAttributes.get(store).sorted().join("\n    "));
        }
    }
}
