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

package com.telenav.mesakit.graph.library.osm.change.io;

import com.telenav.kivakit.filesystem.File;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.library.osm.change.ConnectionPoint;
import com.telenav.mesakit.graph.library.osm.change.MutableWay;
import com.telenav.mesakit.graph.library.osm.change.NewWay;
import com.telenav.mesakit.graph.library.osm.change.store.ModifiedWayStore;
import com.telenav.mesakit.graph.library.osm.change.store.NewWayStore;
import com.telenav.mesakit.graph.library.osm.change.store.PbfNodeStore;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;

public class JosmChangeFile
{
    private PbfNodeStore nodes;

    private ModifiedWayStore modifiedWays;

    private NewWayStore newWays;

    public void add(final NewWay way)
    {
        newWays.add(way);
    }

    public void connectFromEnd(final ConnectionPoint connection, final MutableWay way)
    {
        way.shape(connection.withFromEndConnected(way.shape()));
    }

    public void connectToEnd(final ConnectionPoint connection, final MutableWay way)
    {
        way.shape(connection.withToEndConnected(way.shape()));
    }

    public MutableWay modifiableWay(final Edge edge)
    {
        return ways(edge.graph()).modifiableWay(edge);
    }

    public MutableWay modifiableWay(final Graph graph, final PbfUserIdentifier userIdentifier,
                                    final PbfUserName userName, final PbfWayIdentifier identifier,
                                    final Polyline shape,
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
