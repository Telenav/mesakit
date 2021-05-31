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

import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.geography.Located;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;

import java.sql.Timestamp;

public abstract class GraphNode extends GraphElement implements Located
{
    public PbfNode asPbfNode()
    {
        return new PbfNode(new Node(commonEntityData(), location().latitude().asDegrees(), location().longitude().asDegrees()));
    }

    @Override
    public int index()
    {
        // With GraphNode objects, the identifier is the index
        return (int) identifierAsLong();
    }

    /**
     * @return True if this vertex or shape point is inside the given bounding rectangle
     */
    @Override
    public boolean isInside(final Rectangle bounds)
    {
        return bounds.contains(location());
    }

    @Override
    public abstract Location location();

    public long locationAsLong()
    {
        return location().asLong();
    }

    @Override
    public abstract MapNodeIdentifier mapIdentifier();

    private CommonEntityData commonEntityData()
    {
        return new CommonEntityData(
                mapIdentifier().asLong(),
                pbfRevisionNumber().asInteger(),
                new Timestamp(lastModificationTime().asMilliseconds()),
                new OsmUser(pbfUserIdentifier().asInteger(), pbfUserName().name()),
                pbfChangeSetIdentifier().asLong(),
                tagList().asList());
    }
}
