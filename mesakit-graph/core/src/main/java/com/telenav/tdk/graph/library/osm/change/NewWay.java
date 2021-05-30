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

package com.telenav.tdk.graph.library.osm.change;

import com.telenav.tdk.core.kernel.language.string.StringList;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.data.formats.library.map.identifiers.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.geography.polyline.Polyline;
import com.telenav.tdk.map.measurements.Angle;
import com.telenav.tdk.map.measurements.Angle.Chirality;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.*;

import static com.telenav.tdk.core.kernel.validation.Validate.ensureNotNull;

/**
 * Represents a new way being added to the base graph
 *
 * @author jonathanl (shibo)
 */
public class NewWay extends MutableWay
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    // The node store to work with
    private final PbfNodeStore nodes;

    // The identifier of this new way
    private final WayIdentifier identifier;

    // The shape of this new way
    private Polyline shape;

    // The list of OSM tags for this new way
    private final PbfTagList tags;

    // True if the new way is a longer version of an existing way
    private final boolean extended;

    public NewWay(final PbfNodeStore nodes, final Polyline shape, final PbfTagList tags, final boolean extended)
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
            tags.add(new Tag(key, value));
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
    }

    /**
     * @param extension The way to add
     * @return A new way representing this way extended by another way (assuming they connect)
     */
    public NewWay connectedTo(final NewWay extension)
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
    public Angle connectionAngle(final NewWay that)
    {
        // If this way's end connects to that way's start
        if (end().equals(that.shape.start()))
        {
            // return the connection angle from last heading to first heading
            final var thisLastHeading = shape.lastSegment().heading();
            final var thatFirstHeading = that.shape.firstSegment().heading();
            return thisLastHeading.difference(thatFirstHeading, Chirality.SMALLEST);
        }
        else
        {
            // other wise the connection is from that to this, so return the connection angle from
            // last heading of that to first heading of this
            final var thatLastHeading = that.shape.lastSegment().heading();
            final var thisFirstHeading = shape.firstSegment().heading();
            return thatLastHeading.difference(thisFirstHeading, Chirality.SMALLEST);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof NewWay)
        {
            final var that = (NewWay) object;
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

    public WayIdentifier identifier()
    {
        return identifier;
    }

    /**
     * @return True if this way is connected to that way
     */
    public boolean isConnected(final NewWay that)
    {
        return shape.isConnectedTo(that.shape);
    }

    /**
     * @return True if this way is the same way as that. Note that presently, this only checks the name for an exact
     * match.
     */
    public boolean isSameWay(final NewWay that)
    {
        final var name = name();
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
    public String pbfTag(final String key)
    {
        for (final var tag : tags)
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
        for (final var location : shape.locationSequence())
        {
            nodes.reference(location);
        }
    }

    public Set<NodeIdentifier> referencedNodes()
    {
        final Set<NodeIdentifier> referenced = new HashSet<>();
        for (final var location : shape.locationSequence())
        {
            final var identifier = nodes.identifier(location);
            if (identifier == null)
            {
                LOGGER.warning("No identifier for location $", location);
            }
            referenced.add(identifier);
        }
        return referenced;
    }

    public void shape(final Polyline shape)
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

    public String toString(final boolean addTelenavTag)
    {
        // Create XML lines list
        final var lines = new StringList();

        // Add open way tag
        lines.add("  <way id=\"" + identifier + "\" visible=\"true\" version=\"1\" timestamp=\"" + new Timestamp()
                + "\" uid=\"2100001\" user=\"scout_osm\">");

        // If there's a way shape
        if (shape != null)
        {
            // add references to each node in the way
            final var identifiers = new PbfNodeIdentifierList(nodes, shape.locationSequence());
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
        for (final var tag : tags)
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
