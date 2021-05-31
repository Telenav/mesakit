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

package com.telenav.kivakit.graph.library.osm.change;

import com.telenav.kivakit.collections.map.ReferenceCountMap;
import com.telenav.kivakit.kernel.language.string.StringList;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.operation.progress.reporters.Progress;
import com.telenav.kivakit.kernel.scalars.counts.Count;
import com.telenav.kivakit.data.formats.library.map.identifiers.*;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.map.geography.Location;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.util.*;

import static com.telenav.kivakit.kernel.validation.Validate.ensureNotNull;

/**
 * Holds OSM nodes being referenced by {@link NewWay} and {@link ModifiableWay} objects.
 *
 * @author jonathanl (shibo)
 */
public class PbfNodeStore
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Location DEBUG_NODE_LOCATION = null;

    // The underlying base graph
    private final Graph base;

    // The next synthetic OSM way/node identifier
    private long nextIdentifier = -1L;

    // Map from location to synthetic OSM node identifier and vice versa
    private final Map<Location, NodeIdentifier> identifierForLocation = new HashMap<>();

    private final TreeMap<NodeIdentifier, Location> locationForIdentifier = new TreeMap<>();

    // Reference count for each location
    private final ReferenceCountMap<Location> referenceCount = new ReferenceCountMap<>();

    /**
     * @param base The base graph
     */
    public PbfNodeStore(final Graph base)
    {
        this.base = ensureNotNull(base);
    }

    /**
     * @return List of nodes in this store as native OSM {@link Node} objects
     */
    public List<PbfNode> asNodes()
    {
        final List<PbfNode> nodes = new ArrayList<>();

        // Go through each identifier
        final var progress = Progress.create(LOGGER);
        for (final var identifier : identifiers())
        {
            // and get the node or shape point as a Node object
            final var location = locationForIdentifier.get(identifier);
            final var point = base.shapePointForLocation(location);
            if (point != null)
            {
                nodes.add(point.asPbfNode());
            }
            else
            {
                LOGGER.warning("Couldn't find shape point for $", location);
            }
            progress.next();
        }
        progress.end();
        return nodes;
    }

    /**
     * @return True if this store contains the given location
     */
    public boolean contains(final Location location)
    {
        return identifierForLocation.containsKey(location);
    }

    /**
     * Decrements the reference count to the given location, removing it if there are no more references
     */
    public void dereference(final Location location)
    {
        referenceCount.dereference(location);
        if (!referenceCount.isReferenced(location))
        {
            final var identifier = identifier(location);
            locationForIdentifier.remove(identifier);
            identifierForLocation.remove(location);
        }
    }

    /**
     * @return The OSM node identifier for the given location
     */
    public NodeIdentifier identifier(final Location location)
    {
        return identifierForLocation.get(location);
    }

    /**
     * @return The {@link NodeIdentifier}s in this store in sorted order.
     */
    public Iterable<NodeIdentifier> identifiers()
    {
        return locationForIdentifier.descendingKeySet();
    }

    /**
     * @return The location for the given identifier if it exists in this store
     */
    public Location location(final NodeIdentifier identifier)
    {
        return locationForIdentifier.get(identifier);
    }

    /**
     * @return The locations in this store
     */
    public Iterable<Location> locations()
    {
        return identifierForLocation.keySet();
    }

    /**
     * @return The next available synthetic node identifier
     */
    public NodeIdentifier nextNodeIdentifier()
    {
        return new PbfNodeIdentifier(nextIdentifier--);
    }

    /**
     * @return The next available synthetic way identifier
     */
    public WayIdentifier nextWayIdentifier()
    {
        return new PbfWayIdentifier(nextIdentifier--);
    }

    /**
     * Increments the reference count to the given location, adding the given location if it doesn't yet exist.
     */
    @SuppressWarnings("ConstantConditions")
    public void reference(final Location location)
    {
        // If we hit the debug node location defined at the top of this file,
        if (location.equals(DEBUG_NODE_LOCATION))
        {
            // break at this line in the debugger (for convenience)
            LOGGER.information("Node location " + location + " referenced");
        }

        // If our store doesn't yet contain the node information
        if (!contains(location))
        {
            // Get any existing identifier for the location
            NodeIdentifier identifier = null;
            final var point = base.shapePointForLocation(location);
            if (point != null)
            {
                identifier = point.mapIdentifier();
            }

            // And if there is no identifier
            if (identifier == null)
            {
                // then store a new synthetic node
                identifier = nextNodeIdentifier();
            }

            // Make association between identifier and location
            identifierForLocation.put(location, identifier);
            locationForIdentifier.put(identifier, location);
        }
        referenceCount.reference(location);
    }

    public Count size()
    {
        return Count.of(locationForIdentifier.size());
    }

    public PbfNodeStore subset(final Set<NodeIdentifier> include)
    {
        // Create subset
        final var subset = new PbfNodeStore(base);

        // Go through the identifiers in this store
        for (final var identifier : identifiers())
        {
            // and if the set of identifiers to include contains the given identifier
            if (include.contains(identifier))
            {
                // then copy the location, identifier and reference count to the subset
                final var location = location(identifier);
                subset.identifierForLocation.put(location, identifier);
                subset.locationForIdentifier.put(identifier, location);
                subset.referenceCount.count(location, referenceCount.count(location));
                subset.nextIdentifier = nextIdentifier;
            }
        }
        return subset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        // Create XML lines list
        final var lines = new StringList();

        // Go through each identifier
        for (final var identifier : identifiers())
        {
            // Get location for identifier
            final var location = location(identifier);

            // Get OSM tags for the location
            var tags = PbfTagList.EMPTY;
            final var point = base.shapePointForLocation(location);
            if (point != null)
            {
                tags = point.tagList();
            }

            // Add open node tag
            lines.add("  <node id=\"" + identifier + "\" lat=\"" + location.latitude().asDegrees() + "\" lon=\""
                    + location.longitude().asDegrees() + "\" timestamp=\"" + new Timestamp()
                    + "\" uid=\"2100001\" user=\"scout_osm\" version=\"1\"" + (tags.isEmpty() ? "/>" : ">"));

            // Add OSM tags
            for (final var tag : tags)
            {
                lines.add("    <tag k=\"" + tag.getKey() + "\" v=\"" + tag.getValue() + "\"/>");
            }

            // If there were any tags, close the node
            if (!tags.isEmpty())
            {
                lines.add("  </node>");
            }
        }

        // Return XML separated by newlines
        return lines.join('\n');
    }
}
