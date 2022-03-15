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

package com.telenav.mesakit.graph.specifications.common.relation.store;

import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.value.count.BitCount;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.primitive.collections.array.packed.SplitPackedArray;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitLongArray;
import com.telenav.kivakit.primitive.collections.map.scalars.LongToIntMap;
import com.telenav.kivakit.primitive.collections.map.scalars.fixed.IntToLongFixedMultiMap;
import com.telenav.kivakit.primitive.collections.map.scalars.fixed.IntToPackedArrayFixedMultiMap;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.EdgeRelationMember;
import com.telenav.mesakit.graph.EdgeRelationMemberRole;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.metadata.DataSpecification.GraphElementFactory;
import com.telenav.mesakit.graph.GraphLimits.Estimated;
import com.telenav.mesakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.mesakit.graph.specifications.common.relation.RelationAttributes;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapRelationIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfIdentifierType;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.road.model.GradeSeparation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.telenav.kivakit.primitive.collections.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;
import static com.telenav.kivakit.validation.ValidationType.VALIDATE_ALL;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

/**
 * Packed relation attributes.
 *
 * @author jonathanl (shibo)
 * @see RelationAttributes
 * @see ArchivedGraphElementStore
 */
@SuppressWarnings("unused")
public class RelationStore extends ArchivedGraphElementStore<EdgeRelation>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final AttributeReference<SplitPackedArray> TYPE =
            new AttributeReference<>(this, RelationAttributes.get().TYPE, "type",
                    () -> (SplitPackedArray) new SplitPackedArray("type")
                            .bits(BitCount._5, NO_OVERFLOW)
                            .nullByte((byte) EdgeRelation.Type.UNKNOWN.identifier())
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitPackedArray type;

    private final AttributeReference<LongToIntMap> MAP_IDENTIFIER =
            new AttributeReference<>(this, RelationAttributes.get().MAP_IDENTIFIER, "mapIdentifierToIdentifier",
                    () -> (LongToIntMap) new LongToIntMap("mapIdentifierToIdentifier")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private LongToIntMap mapIdentifierToIdentifier;

    private final AttributeReference<IntToLongFixedMultiMap> MEMBER_IDENTIFIERS =
            new AttributeReference<>(this, RelationAttributes.get().MEMBER_IDENTIFIERS, "memberIdentifiers",
                    () -> (IntToLongFixedMultiMap) new IntToLongFixedMultiMap("memberIdentifiers")
                            .initialChildSize(Estimated.EDGES_PER_RELATION)
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private IntToLongFixedMultiMap memberIdentifiers;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> MEMBER_ROLES =
            new AttributeReference<>(this, RelationAttributes.get().MEMBER_ROLES, "memberRoles",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("memberRoles")
                            .bits(BitCount._5, NO_OVERFLOW)
                            .listTerminator(31)
                            .initialChildSize(Estimated.EDGES_PER_RELATION)
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private IntToPackedArrayFixedMultiMap memberRoles;

    private final AttributeReference<SplitLongArray> VIA_NODE_LOCATION =
            new AttributeReference<>(this, RelationAttributes.get().VIA_NODE_LOCATION, "viaNodeLocation",
                    () -> (SplitLongArray) new SplitLongArray("viaNodeLocation")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitLongArray viaNodeLocation;

    public RelationStore(Graph graph)
    {
        super(graph);
    }

    public Count add(Iterable<? extends EdgeRelation> relations, GraphConstraints constraints)
    {
        var count = 0;

        var adder = adder();
        for (EdgeRelation relation : relations)
        {
            if (constraints.includes(relation))
            {
                var route = relation.asRoute();
                if (route != null)
                {
                    adder.add(relation);
                    edgeStore().storeRelation(route.first(), relation);
                    count++;
                }
            }
        }
        return Count.count(count);
    }

    public boolean contains(MapRelationIdentifier identifier)
    {
        MAP_IDENTIFIER.load();
        return mapIdentifierToIdentifier.containsKey(identifier.asLong());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimate initialSize()
    {
        return metadata().relationCount(ALLOW_ESTIMATE).asEstimate();
    }

    @Override
    public String objectName()
    {
        return "relation-store";
    }

    public EdgeRelation relationForIdentifier(MapRelationIdentifier identifier)
    {
        MAP_IDENTIFIER.load();
        var relationIdentifier = mapIdentifierToIdentifier.get(identifier.asLong());
        if (!mapIdentifierToIdentifier.isNull(relationIdentifier))
        {
            return dataSpecification().newRelation(graph(), relationIdentifier);
        }
        return null;
    }

    public List<EdgeRelationMember> retrieveMembers(EdgeRelation relation)
    {
        MEMBER_ROLES.load();
        MEMBER_IDENTIFIERS.load();
        var identifiers = memberIdentifiers.get(relation.index());
        var roles = roles(relation.index());
        if (identifiers != null && roles != null)
        {
            if (identifiers.size() != roles.size())
            {
                DEBUG.warning("Relation $ has $ identifiers but $ roles", relation, identifiers.size(), roles.size());
            }
            List<EdgeRelationMember> members = new ArrayList<>();
            for (var index = 0; index < identifiers.size(); index++)
            {
                var identifier = PbfIdentifierType.forIdentifierAndType(identifiers.get(index));
                if (identifier instanceof MapNodeIdentifier || identifier instanceof MapWayIdentifier)
                {
                    long code = roles.get(index);
                    members.add(new EdgeRelationMember(relation, identifier, EdgeRelationMemberRole.of((int) code).name()));
                }
            }
            return members;
        }
        else
        {
            if (identifiers == null)
            {
                DEBUG.warning("Relation $ was stored without identifiers", relation);
            }
            if (roles == null)
            {
                DEBUG.warning("Relation $ was stored without roles", relation);
            }
        }
        return Collections.emptyList();
    }

    public EdgeRelation.Type retrieveType(EdgeRelation relation)
    {
        return TYPE.retrieveObject(relation, value -> EdgeRelation.Type.forIdentifier((int) value));
    }

    public Location retrieveViaNodeLocation(EdgeRelation relation)
    {
        return VIA_NODE_LOCATION.retrieveObject(relation, Location::dm7);
    }

    /**
     * Stores all the simple attributes of the given relation at the given edge index
     */
    public void storeAttributes(EdgeRelation relation)
    {
        super.storeAttributes(relation);

        // Store the via node location, if any,
        storeViaNodeLocation(relation);
    }

    public void storeViaNodeLocation(EdgeRelation relation)
    {
        VIA_NODE_LOCATION.allocate();

        var via = relation.memberInRole("via");
        if (via != null)
        {
            Location location = null;
            if (via.isNode())
            {
                location = via.location();
            }
            else
            {
                var firstEdge = via.firstEdge();
                if (firstEdge != null)
                {
                    location = firstEdge.toLocation();
                }
            }
            if (location != null)
            {
                viaNodeLocation.set(relation.index(), location.asDm7Long());
            }
        }
    }

    @Override
    public Validator validator(ValidationType validation)
    {
        var outer = this;

        var validator = validation == VALIDATE_ALL;

        return !validator ? Validator.NULL : new StoreValidator()
        {
            @Override
            protected void onValidate()
            {
                if (outer.isEmpty())
                {
                    warning("Relation store is empty");
                }
                else
                {
                    validate(RelationStore.super.validator(validation));
                }
            }
        };
    }

    @Override
    protected GraphElementFactory<EdgeRelation> elementFactory()
    {
        return dataSpecification()::newRelation;
    }

    @Override
    protected Class<EdgeRelation> elementType()
    {
        return EdgeRelation.class;
    }

    @Override
    protected synchronized void onAdd(EdgeRelation relation)
    {
        // Classify the relation using tags and members
        var type = relation.classify();

        // and if it's a grade separation,
        if (type == EdgeRelation.Type.GRADE_SEPARATION)
        {
            // then separate the vertexes of the relation by grade
            gradeSeparate(relation);
        }
        else
        {
            // Get the relation identifier
            var identifier = relation.identifierAsLong();

            // and if we haven't already stored a relation with this identifier
            // (some erroneous PBF input files may contain duplicate relations)
            if (!containsIdentifier(identifier))
            {
                // assign an index to the relation identifier,
                var index = identifierToIndex(identifier, IndexingMode.CREATE);
                relation.index(index);

                // call the superclass,
                super.onAdd(relation);

                // store the edge relation type,
                TYPE.allocate();
                this.type.set(relation.index(), (byte) type.identifier());

                // store the map identifier --> relation identifier mapping,
                MAP_IDENTIFIER.allocate();
                mapIdentifierToIdentifier.put(relation.mapIdentifier().asLong(), (int) identifier);

                // store the relation attributes and via node,
                storeAttributes(relation);

                // and finally, the members.
                storeMembers(relation);

                if (DEBUG.isDebugOn())
                {
                    var retrieved = dataSpecification().newRelation(graph(), identifier);
                    assert retrieved.validator(VALIDATE_ALL).validate(LOGGER);
                    assert relation.equals(retrieved);
                }
            }
        }
    }

    /**
     * Separates the vertex and the edges connected to it that are at the same grade. The separated vertex will have a
     * slightly perturbed location to ensure that it doesn't precisely overlap vertexes at other grades (which can cause
     * various problems).
     *
     * @param edgesAtGrade The edges that are at the given grade
     * @param vertex The vertex to grade-separate
     * @param grade The grade of the vertexes and the edges in the given route
     */
    private void gradeSeparate(Vertex vertex, GradeSeparation grade, EdgeSet edgesAtGrade)
    {
        // Get the edges connected to the vertex that at the given grade
        var edges = vertex.edges().intersection(edgesAtGrade);

        // then grade separate the vertex and its edges
        vertexStore().gradeSeparate(vertex, grade, edges);
    }

    /**
     * Grade separates the vertexes of all member edges. The grade level is specified by the members role. A new vertex
     * with a perturbed location is created for each vertex that is grade-separated.
     *
     * @param relation The relation of edges which need to be grade separated
     */
    private void gradeSeparate(EdgeRelation relation)
    {
        // Go through the relation's members
        var edgesAtGrade = new HashMap<GradeSeparation, EdgeSet>();
        for (var member : relation.members())
        {
            // get the member's grade level
            var level = Ints.parseInt(this, member.role());
            if (level != Ints.INVALID)
            {
                // and add to the set of edges at that level
                var route = member.route();
                if (route != null)
                {
                    var grade = GradeSeparation.of(level);
                    var existing = edgesAtGrade.computeIfAbsent(grade, ignored -> new EdgeSet());
                    existing.add(route);
                    for (var edge : route)
                    {
                        existing.add(edge.forward());
                    }
                }
                else
                {
                    DEBUG.trace("Bad grade-separation: Unable to find member $ of relation $", member, relation);
                    return;
                }
            }
        }

        // Go through each grade
        for (var grade : edgesAtGrade.keySet())
        {
            // get the edges at that grade
            var edges = edgesAtGrade.get(grade);

            // and if there is a shared vertex,
            var vertex = edges.sharedVertex();
            if (vertex != null)
            {
                // grade-separate the vertex
                gradeSeparate(vertex, grade, edges);
            }
            else
            {
                DEBUG.trace("Edges at grade $ of grade-separation relation $ have no shared vertex: $", grade, relation, edges);
                return;
            }
        }
    }

    private LongArray roles(int index)
    {
        MEMBER_ROLES.load();
        return memberRoles.get(index);
    }

    private void storeMembers(EdgeRelation relation)
    {
        MEMBER_ROLES.allocate();
        MEMBER_IDENTIFIERS.allocate();

        // If we have relation members
        var members = relation.members();
        if (members != null && !members.isEmpty())
        {
            // go through each member and store their roles and identifiers
            var identifiers = new LongArray(objectName() + ".identifiers");
            identifiers.initialSize(Estimated.MEMBERS_PER_RELATION);
            identifiers.initialize();

            var roles = new LongArray(objectName() + ".roles");
            roles.nullLong(31);
            roles.initialSize(Estimated.MEMBERS_PER_RELATION);
            roles.initialize();

            for (var member : members)
            {
                if (member != null)
                {
                    identifiers.add(((PbfIdentifierType) member.identifier()).withType().asLong());
                    var role = EdgeRelationMemberRole.of(member.role());
                    roles.add(role == null ? EdgeRelationMemberRole.EMPTY.code() : (byte) role.code());
                }
            }

            assert roles.size() == identifiers.size();

            memberRoles.putAll(relation.index(), roles);
            memberIdentifiers.putAll(relation.index(), identifiers);
        }
    }
}
