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

package com.telenav.kivakit.graph.specifications.common.relation.store;

import com.telenav.kivakit.collections.primitive.array.packed.SplitPackedArray;
import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.collections.primitive.map.multi.fixed.*;
import com.telenav.kivakit.collections.primitive.map.scalars.LongToIntMap;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.language.primitive.Ints;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.kernel.validation.*;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.data.formats.library.map.identifiers.*;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfIdentifierType;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.collections.EdgeSet;
import com.telenav.kivakit.graph.identifiers.RelationIdentifier;
import com.telenav.kivakit.graph.io.load.GraphConstraints;
import com.telenav.kivakit.graph.metadata.DataSpecification.GraphElementFactory;
import com.telenav.kivakit.graph.project.KivaKitGraphCoreLimits.Estimated;
import com.telenav.kivakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.kivakit.graph.specifications.common.relation.RelationAttributes;
import com.telenav.kivakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.road.model.GradeSeparation;

import java.util.*;

import static com.telenav.kivakit.collections.primitive.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;
import static com.telenav.kivakit.kernel.validation.Validation.VALIDATE_ALL;
import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

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
                            .bits(Bits._5, NO_OVERFLOW)
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
                            .bits(Bits._5, NO_OVERFLOW)
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

    public RelationStore(final Graph graph)
    {
        super(graph);
    }

    public Count add(final Iterable<? extends EdgeRelation> relations, final GraphConstraints constraints)
    {
        var count = 0;

        final var adder = adder();
        for (final EdgeRelation relation : relations)
        {
            if (constraints.includes(relation))
            {
                final var route = relation.asRoute();
                if (route != null)
                {
                    adder.add(relation);
                    edgeStore().storeRelation(route.first(), relation);
                    count++;
                }
            }
        }
        return Count.of(count);
    }

    public boolean contains(final RelationIdentifier identifier)
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

    public EdgeRelation relationForIdentifier(final MapRelationIdentifier identifier)
    {
        MAP_IDENTIFIER.load();
        final var relationIdentifier = mapIdentifierToIdentifier.get(identifier.asLong());
        if (!mapIdentifierToIdentifier.isNull(relationIdentifier))
        {
            return dataSpecification().newRelation(graph(), relationIdentifier);
        }
        return null;
    }

    public List<EdgeRelationMember> retrieveMembers(final EdgeRelation relation)
    {
        MEMBER_ROLES.load();
        MEMBER_IDENTIFIERS.load();
        final var identifiers = memberIdentifiers.get(relation.index());
        final var roles = roles(relation.index());
        if (identifiers != null && roles != null)
        {
            if (identifiers.size() != roles.size())
            {
                DEBUG.warning("Relation $ has $ identifiers but $ roles", relation, identifiers.size(), roles.size());
            }
            final List<EdgeRelationMember> members = new ArrayList<>();
            for (var index = 0; index < identifiers.size(); index++)
            {
                final var identifier = PbfIdentifierType.forIdentifierAndType(identifiers.get(index));
                if (identifier instanceof NodeIdentifier || identifier instanceof WayIdentifier)
                {
                    final long code = roles.get(index);
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

    public EdgeRelation.Type retrieveType(final EdgeRelation relation)
    {
        return TYPE.retrieveObject(relation, value -> EdgeRelation.Type.forIdentifier((int) value));
    }

    public Location retrieveViaNodeLocation(final EdgeRelation relation)
    {
        return VIA_NODE_LOCATION.retrieveObject(relation, Location::dm7);
    }

    /**
     * Stores all of the simple attributes of the given relation at the given edge index
     */
    public void storeAttributes(final EdgeRelation relation)
    {
        super.storeAttributes(relation);

        // Store the via node location, if any,
        storeViaNodeLocation(relation);
    }

    public void storeViaNodeLocation(final EdgeRelation relation)
    {
        VIA_NODE_LOCATION.allocate();

        final var via = relation.memberInRole("via");
        if (via != null)
        {
            Location location = null;
            if (via.isNode())
            {
                location = via.location();
            }
            else
            {
                final var firstEdge = via.firstEdge();
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
    public Validator validator(final Validation validation)
    {
        final var outer = this;

        final var validator = validation == VALIDATE_ALL;

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
    protected synchronized void onAdd(final EdgeRelation relation)
    {
        // Classify the relation using tags and members
        final var type = relation.classify();

        // and if it's a grade separation,
        if (type == EdgeRelation.Type.GRADE_SEPARATION)
        {
            // then separate the vertexes of the relation by grade
            gradeSeparate(relation);
        }
        else
        {
            // Get the relation identifier
            final var identifier = relation.identifierAsLong();

            // and if we haven't already stored a relation with this identifier
            // (some erroneous PBF input files may contain duplicate relations)
            if (!containsIdentifier(identifier))
            {
                // assign an index to the relation identifier,
                final var index = identifierToIndex(identifier, IndexingMode.CREATE);
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

                if (DEBUG.isEnabled())
                {
                    final var retrieved = dataSpecification().newRelation(graph(), identifier);
                    assert retrieved.validator(VALIDATE_ALL).isValid(LOGGER);
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
    private void gradeSeparate(final Vertex vertex, final GradeSeparation grade, final EdgeSet edgesAtGrade)
    {
        // Get the edges connected to the vertex that at the given grade
        final var edges = vertex.edges().intersection(edgesAtGrade);

        // then grade separate the vertex and its edges
        vertexStore().gradeSeparate(vertex, grade, edges);
    }

    /**
     * Grade separates the vertexes of all member edges. The grade level is specified by the members role. A new vertex
     * with a perturbed location is created for each vertex that is grade-separated.
     *
     * @param relation The relation of edges which need to be grade separated
     */
    private void gradeSeparate(final EdgeRelation relation)
    {
        // Go through the relation's members
        final var edgesAtGrade = new HashMap<GradeSeparation, EdgeSet>();
        for (final var member : relation.members())
        {
            // get the member's grade level
            final var level = Ints.parse(member.role());
            if (level != Ints.INVALID)
            {
                // and add to the set of edges at that level
                final var route = member.route();
                if (route != null)
                {
                    final var grade = GradeSeparation.of(level);
                    final var existing = edgesAtGrade.computeIfAbsent(grade, ignored -> new EdgeSet());
                    existing.add(route);
                    for (final var edge : route)
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
        for (final var grade : edgesAtGrade.keySet())
        {
            // get the edges at that grade
            final var edges = edgesAtGrade.get(grade);

            // and if there is a shared vertex,
            final var vertex = edges.sharedVertex();
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

    private LongArray roles(final int index)
    {
        MEMBER_ROLES.load();
        return memberRoles.get(index);
    }

    private void storeMembers(final EdgeRelation relation)
    {
        MEMBER_ROLES.allocate();
        MEMBER_IDENTIFIERS.allocate();

        // If we have relation members
        final var members = relation.members();
        if (members != null && !members.isEmpty())
        {
            // go through each member and store their roles and identifiers
            final var identifiers = new LongArray(objectName() + ".identifiers");
            identifiers.initialSize(Estimated.MEMBERS_PER_RELATION);
            identifiers.initialize();

            final var roles = new LongArray(objectName() + ".roles");
            roles.nullLong(31);
            roles.initialSize(Estimated.MEMBERS_PER_RELATION);
            roles.initialize();

            for (final var member : members)
            {
                if (member != null)
                {
                    identifiers.add(((PbfIdentifierType) member.identifier()).withType().asLong());
                    final var role = EdgeRelationMemberRole.of(member.role());
                    roles.add(role == null ? EdgeRelationMemberRole.EMPTY.code() : (byte) role.code());
                }
            }

            assert roles.size() == identifiers.size();

            memberRoles.putAll(relation.index(), roles);
            memberIdentifiers.putAll(relation.index(), identifiers);
        }
    }
}
