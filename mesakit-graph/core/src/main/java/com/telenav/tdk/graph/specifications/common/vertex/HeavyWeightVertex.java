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

package com.telenav.tdk.graph.specifications.common.vertex;

import com.telenav.tdk.core.collections.set.operations.Union;
import com.telenav.tdk.core.kernel.language.reflection.property.filters.TdkIncludeProperty;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.tdk.data.formats.pbf.model.change.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.collections.EdgeSet;
import com.telenav.tdk.graph.identifiers.VertexIdentifier;
import com.telenav.tdk.graph.metadata.DataSpecification;
import com.telenav.tdk.graph.project.TdkGraphCoreLimits.*;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.road.model.GradeSeparation;

import static com.telenav.tdk.core.kernel.validation.Validate.ensureNotNull;

public class HeavyWeightVertex extends Vertex
{
    private Location location;

    private EdgeSet inEdges = new EdgeSet(Limit.EDGES_PER_VERTEX, Estimated.EDGES_PER_VERTEX);

    private EdgeSet outEdges = new EdgeSet(Limit.EDGES_PER_VERTEX, Estimated.EDGES_PER_VERTEX);

    private NodeIdentifier nodeIdentifier;

    private PbfChangeSetIdentifier pbfChangeSetIdentifier;

    private Time pbfLastModificationTime;

    private PbfRevisionNumber pbfRevisionNumber;

    private PbfTagList pbfTags = PbfTagList.EMPTY;

    private PbfUserIdentifier pbfUserIdentifier;

    private PbfUserName pbfUserName;

    private int index;

    private GradeSeparation grade;

    private boolean isClipped;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightVertex(final Graph graph, final VertexIdentifier identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightVertex(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    public void addIn(final Edge in)
    {
        ensureNotNull(in);
        inEdges.add(in);
    }

    public void addOut(final Edge out)
    {
        ensureNotNull(out);
        outEdges.add(out);
    }

    public void clipped(final boolean clipped)
    {
        isClipped = clipped;
    }

    public void copy(final Vertex that)
    {
        location(that.location());
        pbfTags = that.tagList();
        nodeIdentifier = that.mapIdentifier();
        inEdges = that.inEdges();
        outEdges = that.outEdges();
    }

    @Override
    public Count edgeCount()
    {
        return Count.of(inEdges.size() + outEdges.size());
    }

    @Override
    public EdgeSet edges()
    {
        return new EdgeSet(Limit.EDGES_PER_VERTEX, Estimated.EDGES_PER_VERTEX, new Union<>(inEdges, outEdges));
    }

    @Override
    public GradeSeparation gradeSeparation()
    {
        return grade;
    }

    public void gradeSeparation(final GradeSeparation grade)
    {
        this.grade = grade;
    }

    @Override
    public Count inEdgeCount()
    {
        return Count.of(inEdges.size());
    }

    @Override
    public EdgeSet inEdges()
    {
        return inEdges;
    }

    @Override
    @TdkIncludeProperty
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
    public boolean isClipped()
    {
        return isClipped;
    }

    @Override
    public boolean isHeavyWeight()
    {
        return true;
    }

    @Override
    public Time lastModificationTime()
    {
        return pbfLastModificationTime;
    }

    @Override
    public Location location()
    {
        return location;
    }

    public void location(final Location location)
    {
        this.location = location;
    }

    @Override
    public NodeIdentifier nodeIdentifier()
    {
        return nodeIdentifier;
    }

    public void nodeIdentifier(final NodeIdentifier nodeIdentifier)
    {
        this.nodeIdentifier = nodeIdentifier;
    }

    @Override
    public Count outEdgeCount()
    {
        return Count.of(outEdges.size());
    }

    @Override
    public EdgeSet outEdges()
    {
        return outEdges;
    }

    @Override
    public PbfChangeSetIdentifier pbfChangeSetIdentifier()
    {
        return pbfChangeSetIdentifier;
    }

    public void pbfChangeSetIdentifier(final PbfChangeSetIdentifier PbfChangeSetIdentifier)
    {
        pbfChangeSetIdentifier = PbfChangeSetIdentifier;
    }

    public void pbfLastModificationTime(final Time lastModified)
    {
        pbfLastModificationTime = lastModified;
    }

    @Override
    public PbfRevisionNumber pbfRevisionNumber()
    {
        return pbfRevisionNumber;
    }

    public void pbfRevisionNumber(final PbfRevisionNumber revision)
    {
        pbfRevisionNumber = revision;
    }

    public void pbfTags(final PbfTagList tags)
    {
        pbfTags = tags;
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
    public PbfTagList tagList()
    {
        return pbfTags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[Vertex identifier = " + identifier() + "]";
    }
}
