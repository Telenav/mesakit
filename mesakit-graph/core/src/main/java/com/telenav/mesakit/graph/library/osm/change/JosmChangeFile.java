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

package com.telenav.mesakit.graph.library.osm.change;

import com.telenav.kivakit.data.formats.library.map.identifiers.PbfWayIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.change.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.kivakit.filesystem.File;
import com.telenav.mesakit.map.geography.polyline.Polyline;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;

public class JosmChangeFile
{
    private PbfNodeStore nodes;

    private ModifiedWayStore modifiedWays;

    private NewWayStore newWays;

    public void add(final NewWay way)
    {
        newWays.add(way);
    }

    public void connectFromEnd(final Connection connection, final ModifiableWay way)
    {
        way.shape(connection.withFromEndConnected(way.shape()));
    }

    public void connectToEnd(final Connection connection, final ModifiableWay way)
    {
        way.shape(connection.withToEndConnected(way.shape()));
    }

    public ModifiableWay modifiableWay(final Edge edge)
    {
        return ways(edge.graph()).modifiableWay(edge);
    }

    public ModifiableWay modifiableWay(final Graph graph, final PbfUserIdentifier userIdentifier,
                                       final PbfUserName userName, final PbfWayIdentifier identifier, final Polyline shape,
                                       final PbfTagList tags,
                                       final int version)
    {
        return ways(graph).modifiableWay(userIdentifier, userName, identifier, shape, tags, version);
    }

    public void save(final File output)
    {
        save(output, false);
    }

    public void save(final File output, final boolean debug)
    {
        final var xml = new JosmXml(output);
        xml.addModifiedWays(modifiedWays.modifiedWays());
        xml.addNewWays(newWays.ways());
        xml.addNodes(nodes);
        xml.save(debug);
    }

    private ModifiedWayStore ways(final Graph graph)
    {
        if (nodes == null)
        {
            nodes = new PbfNodeStore(graph);
            modifiedWays = new ModifiedWayStore(nodes);
            newWays = new NewWayStore();
        }
        return modifiedWays;
    }
}
