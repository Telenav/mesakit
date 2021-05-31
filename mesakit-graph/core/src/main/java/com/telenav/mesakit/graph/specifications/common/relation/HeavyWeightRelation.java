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

package com.telenav.mesakit.graph.specifications.common.relation;

import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.EdgeRelationMember;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HeavyWeightRelation extends EdgeRelation
{
    private List<EdgeRelationMember> members = new ArrayList<>();

    private Location viaNodeLocation;

    private Rectangle bounds;

    private PbfChangeSetIdentifier pbfChangeSetIdentifier;

    private Time lastModificationTime;

    private PbfRevisionNumber pbfRevisionNumber;

    private PbfTagList tags = PbfTagList.EMPTY;

    private PbfUserIdentifier pbfUserIdentifier;

    private PbfUserName pbfUserName;

    private int index;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightRelation(final Graph graph, final RelationIdentifier identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightRelation(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    /**
     * <b>Not Public API</b>
     */
    public HeavyWeightRelation(final PbfRelation relation)
    {
        this(null, relation.identifierAsLong());

        tags = relation.tagList();
        assert tags.isValid();

        final var time = relation.get().getTimestamp().getTime();
        if (time > 0)
        {
            lastModificationTime = Time.milliseconds(time);
        }

        pbfRevisionNumber = new PbfRevisionNumber(relation.version());
        pbfChangeSetIdentifier = new PbfChangeSetIdentifier(relation.changeSetIdentifier());
        pbfUserIdentifier = new PbfUserIdentifier(relation.user().getId());
        pbfUserName = new PbfUserName(relation.user().getName());

        members = members(this, relation);
    }

    public void add(final Edge edge, final String role)
    {
        members.add(new EdgeRelationMember(this, edge.mapIdentifier(), role));
    }

    @Override
    public Rectangle bounds()
    {
        if (bounds == null)
        {
            if (members != null && !members.isEmpty())
            {
                final var builder = new BoundingBoxBuilder();
                for (final var member : members())
                {
                    builder.add(member.element().bounds());
                }
                bounds = builder.build();
            }
        }
        return bounds;
    }

    public void bounds(final Rectangle bounds)
    {
        this.bounds = bounds;
    }

    public HeavyWeightRelation copy()
    {
        final var copy = dataSpecification().newHeavyWeightRelation(graph(), identifierAsLong());
        copy.copy(this);
        return copy;
    }

    public void copy(final EdgeRelation that)
    {
        index(that.index());
        bounds(that.bounds());
        members(new ArrayList<>(that.members()));
        tags(that.tagList());
        pbfChangeSetIdentifier(that.pbfChangeSetIdentifier());
        pbfRevisionNumber(that.pbfRevisionNumber());
        pbfUserIdentifier(that.pbfUserIdentifier());
        pbfUserName(that.pbfUserName());
        lastModificationTime(that.lastModificationTime());
    }

    @Override
    public int index()
    {
        return index;
    }

    @Override
    public void index(final int index)
    {
        this.index = index;
    }

    @Override
    public boolean isHeavyWeight()
    {
        return true;
    }

    @Override
    public Time lastModificationTime()
    {
        return lastModificationTime;
    }

    public void lastModificationTime(final Time lastModified)
    {
        lastModificationTime = lastModified;
    }

    @Override
    public List<EdgeRelationMember> members()
    {
        return Objects.requireNonNullElse(members, Collections.emptyList());
    }

    public void members(final List<EdgeRelationMember> members)
    {
        this.members = members;
    }

    @Override
    public PbfChangeSetIdentifier pbfChangeSetIdentifier()
    {
        return pbfChangeSetIdentifier;
    }

    public void pbfChangeSetIdentifier(final PbfChangeSetIdentifier pbfChangeSetIdentifier)
    {
        this.pbfChangeSetIdentifier = pbfChangeSetIdentifier;
    }

    @Override
    public PbfRevisionNumber pbfRevisionNumber()
    {
        return pbfRevisionNumber;
    }

    public void pbfRevisionNumber(final PbfRevisionNumber pbfRevisionNumber)
    {
        this.pbfRevisionNumber = pbfRevisionNumber;
    }

    @Override
    public PbfUserIdentifier pbfUserIdentifier()
    {
        return pbfUserIdentifier;
    }

    public void pbfUserIdentifier(final PbfUserIdentifier PbfUserIdentifier)
    {
        pbfUserIdentifier = PbfUserIdentifier;
    }

    @Override
    public PbfUserName pbfUserName()
    {
        return pbfUserName;
    }

    public void pbfUserName(final PbfUserName PbfUserName)
    {
        pbfUserName = PbfUserName;
    }

    @Override
    public List<PbfWayIdentifier> pbfWayIdentifiers()
    {
        if (members == null)
        {
            return Collections.emptyList();
        }
        final List<PbfWayIdentifier> identifiers = new ArrayList<>();
        for (final var member : members)
        {
            if (member.isWay())
            {
                identifiers.add(member.route().first().wayIdentifier());
            }
        }
        return identifiers;
    }

    @Override
    public PbfTagList tagList()
    {
        return tags;
    }

    public void tags(final PbfTagList tags)
    {
        assert tags.isValid();
        this.tags = tags;
    }

    @Override
    public Location viaNodeLocation()
    {
        return viaNodeLocation;
    }

    public void viaNodeLocation(final Location location)
    {
        viaNodeLocation = location;
    }

    private List<EdgeRelationMember> members(final EdgeRelation relation, final PbfRelation pbfRelation)
    {
        final List<EdgeRelationMember> from = new ArrayList<>();
        final List<EdgeRelationMember> to = new ArrayList<>();
        final List<EdgeRelationMember> via = new ArrayList<>();
        for (final var member : pbfRelation.members())
        {
            var role = member.getMemberRole();
            if (Strings.isEmpty(role))
            {
                role = null;
            }
            if (member.getMemberType() == EntityType.Way)
            {
                final var wayIdentifier = new PbfWayIdentifier(member.getMemberId());
                final var relationMember = new EdgeRelationMember(relation, wayIdentifier, role);
                if ("from".equalsIgnoreCase(role))
                {
                    from.add(relationMember);
                }
                else if ("via".equalsIgnoreCase(role))
                {
                    via.add(relationMember);
                }
                else if ("to".equalsIgnoreCase(role))
                {
                    to.add(relationMember);
                }
                else
                {
                    // handles cases other than turn restrictions
                    to.add(relationMember);
                }
            }
            if (member.getMemberType() == EntityType.Node)
            {
                if ("via".equalsIgnoreCase(role))
                {
                    final var nodeIdentifier = new PbfNodeIdentifier(member.getMemberId());
                    final var relationMember = new EdgeRelationMember(relation, nodeIdentifier, role);
                    via.add(relationMember);
                }
            }
        }
        final List<EdgeRelationMember> members = new ArrayList<>();
        members.addAll(from);
        members.addAll(via);
        members.addAll(to);
        return members;
    }
}
