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

package com.telenav.mesakit.graph.specifications.common.vertex;

import com.telenav.kivakit.collections.set.logical.operations.Union;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.road.model.GradeSeparation;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;
import static com.telenav.mesakit.graph.project.GraphCoreLimits.Estimated;
import static com.telenav.mesakit.graph.project.GraphCoreLimits.Limit;

public class HeavyWeightVertex extends Vertex
{
    private Location location;

    private EdgeSet inEdges = new EdgeSet(Limit.EDGES_PER_VERTEX, Estimated.EDGES_PER_VERTEX);

    private EdgeSet outEdges = new EdgeSet(Limit.EDGES_PER_VERTEX, Estimated.EDGES_PER_VERTEX);

    private MapNodeIdentifier nodeIdentifier;

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
        return Count.count(inEdges.size() + outEdges.size());
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
        return Count.count(inEdges.size());
    }

    @Override
    public EdgeSet inEdges()
    {
        return inEdges;
    }

    @Override
    @KivaKitIncludeProperty
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
    public MapNodeIdentifier nodeIdentifier()
    {
        return nodeIdentifier;
    }

    public void nodeIdentifier(final MapNodeIdentifier nodeIdentifier)
    {
        this.nodeIdentifier = nodeIdentifier;
    }

    @Override
    public Count outEdgeCount()
    {
        return Count.count(outEdges.size());
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