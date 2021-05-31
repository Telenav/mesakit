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

package com.telenav.mesakit.graph.specifications.common.vertex.store;

import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.GraphNode;
import com.telenav.mesakit.graph.identifiers.GraphElementIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

public class GraphNodeIndex extends GraphNode
{
    private final int index;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public GraphNodeIndex(final int index)
    {
        this.index = index;
    }

    @Override
    public GraphElement asHeavyWeight()
    {
        return unsupported();
    }

    @Override
    public GraphElementAttributes<?> attributes()
    {
        return unsupported();
    }

    @Override
    public Rectangle bounds()
    {
        return unsupported();
    }

    @Override
    public Graph graph()
    {
        return unsupported();
    }

    @Override
    public GraphElementIdentifier identifier()
    {
        return unsupported();
    }

    @Override
    public int index()
    {
        return index;
    }

    @Override
    public Time lastModificationTime()
    {
        return unsupported();
    }

    @Override
    public Location location()
    {
        return unsupported();
    }

    @Override
    public MapNodeIdentifier mapIdentifier()
    {
        return unsupported();
    }

    @Override
    public PbfChangeSetIdentifier pbfChangeSetIdentifier()
    {
        return unsupported();
    }

    @Override
    public PbfRevisionNumber pbfRevisionNumber()
    {
        return unsupported();
    }

    @Override
    public PbfUserIdentifier pbfUserIdentifier()
    {
        return unsupported();
    }

    @Override
    public PbfUserName pbfUserName()
    {
        return unsupported();
    }

    @Override
    public GraphElementPropertySet<GraphNodeIndex> properties()
    {
        return unsupported();
    }

    @Override
    public PbfTagList tagList()
    {
        return unsupported();
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected GraphElementStore store()
    {
        return unsupported();
    }
}
