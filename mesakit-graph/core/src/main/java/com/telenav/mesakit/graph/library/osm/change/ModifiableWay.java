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

import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.objects.Objects;
import com.telenav.kivakit.kernel.language.strings.Escape;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.Edge;
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

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * A class that holds an OSM way and models any changes that need to be made to nodes that are caused by adding new
 * connections to the way (sometimes a connection can be made to an existing node and sometimes a new node needs to be
 * added because there is no node reasonably close). In addition, holds any road name that has been added if the way is
 * unnamed.
 *
 * @author jonathanl (shibo)
 */
public class ModifiableWay extends MutableWay
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
    public ModifiableWay(final PbfNodeStore nodes, final Edge edge)
    {
        this.nodes = nodes;
        identifier = edge.wayIdentifier();
        shape = edge.wayAsRoute().polyline();
        tags = edge.tagList();
        version = edge.pbfRevisionNumber().asInteger();
        userIdentifier = edge.pbfUserIdentifier();
        userName = edge.pbfUserName();
        changeSetIdentifier = edge.pbfChangeSetIdentifier();

        if (userName == null)
        {
            userName = new PbfUserName("telenav-cygnus-plus");
        }
    }

    public ModifiableWay(final PbfNodeStore nodes, final PbfUserIdentifier userIdentifier, final PbfUserName userName,
                         final PbfWayIdentifier identifier, final Polyline shape, final PbfTagList tags,
                         final int version)
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
    public void addNewTags(final PbfTagList tags)
    {
        for (final var tag : tags)
        {
            addTag(tag.getKey(), tag.getValue());
        }
    }

    @Override
    public void addTag(final String key, final String value)
    {
        if (!tags.containsKey(key))
        {
            modified("added-tag(" + key + "='" + value + "')");
            tags.add(new Tag(key, value));
        }
    }

    public PbfWay asPbfWay()
    {
        final var data = new CommonEntityData(identifier.asLong(), version, new Timestamp(), user(),
                changeSetIdentifier.asLong(), tags.asList());
        final List<WayNode> wayNodes = new ArrayList<>();
        for (final var location : shape.locationSequence())
        {
            wayNodes.add(new WayNode(nodes.identifier(location).asLong()));
        }
        return new PbfWay(new Way(data, wayNodes));
    }

    public void changeSetIdentifier(final PbfChangeSetIdentifier changeSetIdentifier)
    {
        if (!Objects.equal(changeSetIdentifier, this.changeSetIdentifier))
        {
            this.changeSetIdentifier = changeSetIdentifier;
            modified("changed-change-set-identifier");
        }
    }

    @Override
    public void changeTag(final String key, final String baseValue, final String enhancingValue)
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
    public Connection connect(final Polyline shape, final Distance maximumDistance, final Connection.End end)
    {
        // If the first/last segment crosses the way, it's a one segment overshoot
        var connection = connect(end == Connection.End.FROM ? shape.firstSegment() : shape.lastSegment(),
                Connection.Type.OVERSHOOT);
        if (connection != null)
        {
            return connection;
        }

        // If the next to first/last segment crosses the way, it's a two segment overshoot
        connection = connect(end == Connection.End.FROM ? shape.secondSegment() : shape.nextToLastSegment(),
                Connection.Type.TWO_SEGMENT_OVERSHOOT);
        if (connection != null)
        {
            return connection;
        }

        // If the extended first/last segment crosses the way, it's an undershoot
        return connect(end == Connection.End.FROM ? shape.firstSegment().withStartExtended(maximumDistance)
                : shape.lastSegment().withEndExtended(maximumDistance), Connection.Type.UNDERSHOOT);
    }

    public PbfWayIdentifier identifier()
    {
        return identifier;
    }

    public boolean isModified()
    {
        return !modifications.isEmpty();
    }

    public void modified(final String modification)
    {
        // If this way hasn't yet been modified,
        if (!isModified())
        {
            // it is being modified for the first time, so reference its nodes
            for (final var location : shape.locationSequence())
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
    public void name(final RoadName name)
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
        final Set<PbfNodeIdentifier> referenced = new HashSet<>();
        for (final var location : shape.locationSequence())
        {
            referenced.add(nodes.identifier(location));
        }
        return referenced;
    }

    public void shape(final Polyline shape)
    {
        if (!Objects.equal(shape, this.shape))
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

    public String toString(final boolean addTelenavTag, final boolean debug)
    {
        // Create XML lines list
        final var lines = new StringList();

        // Add way open tag
        lines.add("  <way id=\"" + identifier + "\" action=\"modify\" visible=\"true\" version=\"" + version
                + "\" timestamp=\"" + new Timestamp() + "\" uid=\"" + userIdentifier + "\" user=\""
                + (userName.name()) + "\">");

        // Add references to nodes
        final var identifiers = new PbfNodeIdentifierList(nodes, shape().locationSequence());
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
        for (final var tag : tags)
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

    public void userIdentifier(final PbfUserIdentifier userIdentifier)
    {
        if (!Objects.equal(userIdentifier, this.userIdentifier))
        {
            this.userIdentifier = userIdentifier;
            modified("changed-user-identifier");
        }
    }

    public void userName(final PbfUserName userName)
    {
        if (!Objects.equal(userName, this.userName))
        {
            this.userName = userName;
            modified("changed-user-name");
        }
    }

    private Connection connect(final Segment segment, final Connection.Type type)
    {
        if (segment != null)
        {
            final var builder = new PolylineBuilder();
            builder.addAll(shape.locationSequence());
            final var intersection = builder.addIntersectionWith(segment, Distance.meters(5));
            if (intersection != null)
            {
                if (intersection.isModified())
                {
                    shape = builder.build();
                    modified("changed-shape(added-new-way-connection at " + intersection.location() + ")");
                }
                nodes.reference(intersection.location());
                return new Connection(intersection.location(), type);
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
        return new OsmUser(userIdentifier.asInteger(), userName.name());
    }
}
