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

package com.telenav.mesakit.graph.specifications.common.place;

import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.identifiers.PlaceIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;

public class HeavyWeightPlace extends Place
{
    private Count population;

    private Location location;

    private String name;

    private Type type;

    private PbfChangeSetIdentifier pbfChangeSetIdentifier;

    private Time lastModificationTime;

    private PbfRevisionNumber pbfRevisionNumber;

    private PbfTagList tags = PbfTagList.EMPTY;

    private PbfUserIdentifier pbfUserIdentifier;

    private PbfUserName pbfUserName;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightPlace(Graph graph, PlaceIdentifier identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightPlace(Graph graph, long identifier)
    {
        super(graph, identifier);
    }

    public void copy(Place that)
    {
        name(that.name());
        type(that.type());
        location(that.location());
        population(that.population());
        tags(that.tagList());
        pbfChangeSetIdentifier(that.pbfChangeSetIdentifier());
        pbfRevisionNumber(that.pbfRevisionNumber());
        pbfUserIdentifier(that.pbfUserIdentifier());
        pbfUserName(that.pbfUserName());
        lastModificationTime(that.lastModificationTime());
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

    public void lastModificationTime(Time lastModified)
    {
        lastModificationTime = lastModified;
    }

    @Override
    public Location location()
    {
        return location;
    }

    public void location(Location location)
    {
        this.location = location;
    }

    @Override
    public String name()
    {
        return name;
    }

    public void name(String name)
    {
        this.name = name;
    }

    @Override
    public PbfChangeSetIdentifier pbfChangeSetIdentifier()
    {
        return pbfChangeSetIdentifier;
    }

    public void pbfChangeSetIdentifier(PbfChangeSetIdentifier PbfChangeSetIdentifier)
    {
        pbfChangeSetIdentifier = PbfChangeSetIdentifier;
    }

    @Override
    public PbfRevisionNumber pbfRevisionNumber()
    {
        return pbfRevisionNumber;
    }

    public void pbfRevisionNumber(PbfRevisionNumber revision)
    {
        pbfRevisionNumber = revision;
    }

    @Override
    public PbfUserIdentifier pbfUserIdentifier()
    {
        return pbfUserIdentifier;
    }

    public void pbfUserIdentifier(PbfUserIdentifier PbfUserIdentifier)
    {
        pbfUserIdentifier = PbfUserIdentifier;
    }

    @Override
    public PbfUserName pbfUserName()
    {
        return pbfUserName;
    }

    public void pbfUserName(PbfUserName PbfUserName)
    {
        pbfUserName = PbfUserName;
    }

    @Override
    public Count population()
    {
        return population;
    }

    public void population(Count population)
    {
        this.population = population;
    }

    @Override
    public PbfTagList tagList()
    {
        return tags;
    }

    public void tags(PbfTagList tags)
    {
        this.tags = tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[Place identifier = " + identifier() + "]";
    }

    @Override
    public Type type()
    {
        return type;
    }

    public void type(Type type)
    {
        this.type = type;
    }
}
