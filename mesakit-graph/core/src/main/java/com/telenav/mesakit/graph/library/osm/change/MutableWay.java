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

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.Objects;
import com.telenav.kivakit.core.string.Escape;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.library.osm.change.store.PbfNodeStore;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.RoadName;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * A class that holds an OSM way and models any changes that need to be made to nodes that are caused by adding new
 * connections to the way (sometimes a connection can be made to an existing node and sometimes a new node needs to be
 * added because there is no node reasonably close). In addition, holds any road name that has been added if the way is
 * unnamed.
 *
 * @author jonathanl (shibo)
 */
public class MutableWay extends BaseMutableWay
{
    // The store of OSM nodes to work with
    private final PbfNodeStore nodes;

    // Tags on this way
    private final PbfTagList tags;

    // The polyline for this way
    private Polyline shape;

    // The version of the OSM way
    private final int version;

    // The name to assign to this way if it is unnamed
    private RoadName name;

    // The way identifier
    private final PbfWayIdentifier identifier;

    // The user identifier
    private PbfUserIdentifier userIdentifier;

    // The user name
    private PbfUserName userName;

    // Change set identifier
    private PbfChangeSetIdentifier changeSetIdentifier;

    // The modification(s) that happened to this way
    private final StringList modifications = new StringList(Maximum._100);

    /**
     * Construct a modified way from a node store and an OSM way.
     *
     * @param nodes The node store to work with
     */
    public MutableWay(PbfNodeStore nodes, Edge edge)
    {
        this.nodes = nodes;
        identifier = edge.wayIdentifier();
        shape = edge.wayAsRoute().polyline();
        tags = edge.tagList();
        version = edge.pbfRevisionNumber().asInt();
        userIdentifier = edge.pbfUserIdentifier();
        userName = edge.pbfUserName();
        changeSetIdentifier = edge.pbfChangeSetIdentifier();

        if (userName == null)
        {
            userName = new PbfUserName("telenav-cygnus-plus");
        }
    }

    public MutableWay(PbfNodeStore nodes, PbfUserIdentifier userIdentifier, PbfUserName userName,
                      PbfWayIdentifier identifier, Polyline shape, PbfTagList tags,
                      int version)
    {
        this.nodes = nodes;
        this.identifier = identifier;
        this.shape = shape;
        this.tags = tags;
        this.version = version;
        this.userIdentifier = userIdentifier;
        this.userName = userName;
    }

    /**
     * Adds any tags from the given list which don't already have a value in the current list of tags for this way
     */
    @Override
    public void addNewTags(PbfTagList tags)
    {
        for (var tag : tags)
        {
            addTag(tag.getKey(), tag.getValue());
        }
    }

    @Override
    public void addTag(String key, String value)
    {
        if (!tags.containsKey(key))
        {
            modified("added-tag(" + key + "='" + value + "')");
            tags.add(new Tag(key, value));
        }
    }

    public PbfWay asPbfWay()
    {
        var data = new CommonEntityData(identifier.asLong(), version, new PbfTimestamp(), user(),
                changeSetIdentifier.asLong(), tags.asList());
        List<WayNode> wayNodes = new ArrayList<>();
        for (var location : shape)
        {
            wayNodes.add(new WayNode(nodes.identifier(location).asLong()));
        }
        return new PbfWay(new Way(data, wayNodes));
    }

    public void changeSetIdentifier(PbfChangeSetIdentifier changeSetIdentifier)
    {
        if (!Objects.isEqual(changeSetIdentifier, this.changeSetIdentifier))
        {
            this.changeSetIdentifier = changeSetIdentifier;
            modified("changed-change-set-identifier");
        }
    }

    @Override
    public void changeTag(String key, String baseValue, String enhancingValue)
    {
        if (tags.containsKey(key))
        {
            tags.removeKey(key);
        }
        tags.add(new Tag(key, enhancingValue));
        modified("changed-tag(" + key + "='" + baseValue + "' -> '" + enhancingValue + "')");
    }

    /**
     * Creates a node to allow a connection at the given location to this OSM way. If there is already a node close
     * enough to the snap location on the way, it will be used. If there is no node close enough, a new node will be
     * added.
     *
     * @param shape The shape of the new way to connect to this modified way
     * @param maximumDistance The maximum distance to look for a connection
     * @return The connection made to this way
     */
    public ConnectionPoint connect(Polyline shape, Distance maximumDistance, ConnectionPoint.End end)
    {
        // If the first/last segment crosses the way, it's a one segment overshoot
        var connection = connect(end == ConnectionPoint.End.FROM ? shape.firstSegment() : shape.lastSegment(),
                ConnectionPoint.Type.OVERSHOOT);
        if (connection != null)
        {
            return connection;
        }

        // If the next to first/last segment crosses the way, it's a two segment overshoot
        connection = connect(end == ConnectionPoint.End.FROM ? shape.secondSegment() : shape.nextToLastSegment(),
                ConnectionPoint.Type.TWO_SEGMENT_OVERSHOOT);
        if (connection != null)
        {
            return connection;
        }

        // If the extended first/last segment crosses the way, it's an undershoot
        return connect(end == ConnectionPoint.End.FROM ? shape.firstSegment().withStartExtended(maximumDistance)
                : shape.lastSegment().withEndExtended(maximumDistance), ConnectionPoint.Type.UNDERSHOOT);
    }

    public PbfWayIdentifier identifier()
    {
        return identifier;
    }

    public boolean isModified()
    {
        return !modifications.isEmpty();
    }

    public void modified(String modification)
    {
        // If this way hasn't yet been modified,
        if (!isModified())
        {
            // it is being modified for the first time, so reference its nodes
            for (var location : shape)
            {
                nodes.reference(location);
            }
        }

        // Add the modification description to the list
        if (!modifications.contains(modification))
        {
            modifications.add(modification);
        }
    }

    /**
     * Names this modified way if it is unnamed
     */
    public void name(RoadName name)
    {
        if (this.name == null && !tags.containsKey("name"))
        {
            this.name = name;
            modified("changed-name");
        }
        else
        {
            fail("Cannot name way that is already named");
        }
    }

    public Set<PbfNodeIdentifier> referencedNodes()
    {
        Set<PbfNodeIdentifier> referenced = new HashSet<>();
        for (var location : shape)
        {
            referenced.add(nodes.identifier(location));
        }
        return referenced;
    }

    public void shape(Polyline shape)
    {
        if (!Objects.isEqual(shape, this.shape))
        {
            this.shape = shape;
            modified("changed-shape");
        }
    }

    /**
     * @return The shape of this modified way
     */
    public Polyline shape()
    {
        return shape;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return toString(false, false);
    }

    public String toString(boolean addTelenavTag, boolean debug)
    {
        // Create XML lines list
        var lines = new StringList();

        // Add way open tag
        lines.add("  <way id=\"" + identifier + "\" action=\"modify\" visible=\"true\" version=\"" + version
                + "\" timestamp=\"" + new PbfTimestamp() + "\" uid=\"" + userIdentifier + "\" user=\""
                + (userName.name()) + "\">");

        // Add references to nodes
        var identifiers = new PbfNodeIdentifierList(nodes, shape());
        lines.addAll(identifiers.references());

        // Add OSM tags
        if (addTelenavTag)
        {
            if (!debug)
            {
                simplifyModifications();
            }
            lines.add("    <tag k=\"telenav:action\" v=\"modified:" + modifications.join(",") + "\"/>");
        }
        for (var tag : tags)
        {
            if (name == null || !"name".equals(tag.getKey()))
            {
                lines.add("    <tag k=\"" + tag.getKey() + "\" v=\"" + Escape.xml(tag.getValue()) + "\"/>");
            }
        }

        // If the way has been named
        if (name != null)
        {
            // add a name tag
            lines.add("    <tag k=\"name\" v=\"" + Escape.xml(name.name()) + "\"/>");
        }

        // Close way tag
        lines.add("  </way>");

        // Return lines separated by newline
        return lines.join('\n');
    }

    public void userIdentifier(PbfUserIdentifier userIdentifier)
    {
        if (!Objects.isEqual(userIdentifier, this.userIdentifier))
        {
            this.userIdentifier = userIdentifier;
            modified("changed-user-identifier");
        }
    }

    public void userName(PbfUserName userName)
    {
        if (!Objects.isEqual(userName, this.userName))
        {
            this.userName = userName;
            modified("changed-user-name");
        }
    }

    private ConnectionPoint connect(Segment segment, ConnectionPoint.Type type)
    {
        if (segment != null)
        {
            var builder = new PolylineBuilder();
            builder.addAll(shape);
            var intersection = builder.addIntersectionWith(segment, Distance.meters(5));
            if (intersection != null)
            {
                if (intersection.isModified())
                {
                    shape = builder.build();
                    modified("changed-shape(added-new-way-connection at " + intersection.location() + ")");
                }
                nodes.reference(intersection.location());
                return new ConnectionPoint(intersection.location(), type);
            }
        }
        return null;
    }

    private void simplifyModifications()
    {
        for (var i = 0; i < modifications.size(); i++)
        {
            modifications.set(i, modifications.get(i).replaceAll("\\(.*?\\)", "/"));
        }
    }

    private OsmUser user()
    {
        return new OsmUser(userIdentifier.asInt(), userName.name());
    }
}
