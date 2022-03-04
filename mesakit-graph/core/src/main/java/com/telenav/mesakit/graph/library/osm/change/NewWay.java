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
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.graph.library.osm.change.store.PbfNodeStore;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;
import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;

/**
 * Represents a new way being added to the base graph
 *
 * @author jonathanl (shibo)
 */

public class NewWay extends BaseMutableWay
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    // The node store to work with
    private final PbfNodeStore nodes;

    // The identifier of this new way
    private final PbfWayIdentifier identifier;

    // The shape of this new way
    private Polyline shape;

    // The list of OSM tags for this new way
    private final PbfTagList tags;

    // True if the new way is a longer version of an existing way
    private final boolean extended;

    public NewWay(PbfNodeStore nodes, Polyline shape, PbfTagList tags, boolean extended)
    {
        this.extended = extended;
        ensureNotNull(shape);
        this.nodes = nodes;
        identifier = nodes.nextWayIdentifier();
        this.shape = shape;
        this.tags = tags;
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
            tags.add(new Tag(key, value));
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
    }

    /**
     * @param extension The way to add
     * @return A new way representing this way extended by another way (assuming they connect)
     */
    public NewWay connectedTo(NewWay extension)
    {
        // If the extension appends to this way
        if (end().equals(extension.start()))
        {
            // then append the polyline
            return new NewWay(nodes, shape().append(extension.shape()), tags, extended);
        }

        // If the extension prepends to this way
        else if (shape.start().equals(extension.shape().end()))
        {
            // then prepend the polyline
            return new NewWay(nodes, extension.shape().append(shape()), tags, extended);
        }

        return null;
    }

    /**
     * @return The connection angle with the given way
     */
    public Angle connectionAngle(NewWay that)
    {
        // If this way's end connects to that way's start
        if (end().equals(that.shape.start()))
        {
            // return the connection angle from last heading to first heading
            var thisLastHeading = shape.lastSegment().heading();
            var thatFirstHeading = that.shape.firstSegment().heading();
            return thisLastHeading.difference(thatFirstHeading, Chirality.SMALLEST);
        }
        else
        {
            // other wise the connection is from that to this, so return the connection angle from
            // last heading of that to first heading of this
            var thatLastHeading = that.shape.lastSegment().heading();
            var thisFirstHeading = shape.firstSegment().heading();
            return thatLastHeading.difference(thisFirstHeading, Chirality.SMALLEST);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof NewWay)
        {
            var that = (NewWay) object;
            return identifier.equals(that.identifier);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return identifier.hashCode();
    }

    public PbfWayIdentifier identifier()
    {
        return identifier;
    }

    /**
     * @return True if this way is connected to that way
     */
    public boolean isConnected(NewWay that)
    {
        return shape.isConnectedTo(that.shape);
    }

    /**
     * @return True if this way is the same way as that. Note that presently, this only checks the name for an exact
     * match.
     */
    public boolean isSameWay(NewWay that)
    {
        var name = name();
        return name != null && name.equals(that.name());
    }

    /**
     * @return The way's name from the OSM name tag
     */
    public String name()
    {
        return pbfTag("name");
    }

    /**
     * @return The OSM tag value for the given key
     */
    public String pbfTag(String key)
    {
        for (var tag : tags)
        {
            if (tag.getKey().equals(key))
            {
                return tag.getValue();
            }
        }
        return null;
    }

    public void referenceNodes()
    {
        for (var location : shape.locationSequence())
        {
            nodes.reference(location);
        }
    }

    public Set<MapNodeIdentifier> referencedNodes()
    {
        Set<MapNodeIdentifier> referenced = new HashSet<>();
        for (var location : shape.locationSequence())
        {
            var identifier = nodes.identifier(location);
            if (identifier == null)
            {
                LOGGER.warning("No identifier for location $", location);
            }
            referenced.add(identifier);
        }
        return referenced;
    }

    public void shape(Polyline shape)
    {
        this.shape = shape;
    }

    /**
     * @return The shape of this new way
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
        return toString(false);
    }

    public String toString(boolean addTelenavTag)
    {
        // Create XML lines list
        var lines = new StringList();

        // Add open way tag
        lines.add("  <way id=\"" + identifier + "\" visible=\"true\" version=\"1\" timestamp=\"" + new PbfTimestamp()
                + "\" uid=\"2100001\" user=\"scout_osm\">");

        // If there's a way shape
        if (shape != null)
        {
            // add references to each node in the way
            var identifiers = new PbfNodeIdentifierList(nodes, shape.locationSequence());
            lines.addAll(identifiers.references());
        }

        // Add OSM tags
        if (addTelenavTag)
        {
            if (extended)
            {
                lines.add("    <tag k=\"telenav:action\" v=\"extended:way\"/>");
            }
            else
            {
                lines.add("    <tag k=\"telenav:action\" v=\"added:way\"/>");
            }
        }
        for (var tag : tags)
        {
            lines.add("    <tag k=\"" + tag.getKey() + "\" v=\"" + tag.getValue() + "\"/>");
        }

        // Close way tag
        lines.add("  </way>");

        // Return XML separated by newlines
        return lines.join('\n');
    }

    private Location end()
    {
        return shape.end();
    }

    private Location start()
    {
        return shape.start();
    }
}
