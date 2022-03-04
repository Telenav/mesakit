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

package com.telenav.mesakit.graph.library.osm.change.store;

import com.telenav.kivakit.core.collections.map.ReferenceCountMap;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.progress.reporters.Progress;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.library.osm.change.MutableWay;
import com.telenav.mesakit.graph.library.osm.change.NewWay;
import com.telenav.mesakit.graph.library.osm.change.PbfTimestamp;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

/**
 * Holds OSM nodes being referenced by {@link NewWay} and {@link MutableWay} objects.
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
    private final Map<Location, PbfNodeIdentifier> identifierForLocation = new HashMap<>();

    private final TreeMap<PbfNodeIdentifier, Location> locationForIdentifier = new TreeMap<>();

    // Reference count for each location
    private final ReferenceCountMap<Location> referenceCount = new ReferenceCountMap<>();

    /**
     * @param base The base graph
     */
    public PbfNodeStore(Graph base)
    {
        this.base = ensureNotNull(base);
    }

    /**
     * @return List of nodes in this store as native OSM {@link Node} objects
     */
    public List<PbfNode> asNodes()
    {
        List<PbfNode> nodes = new ArrayList<>();

        // Go through each identifier
        var progress = Progress.create(LOGGER);
        for (var identifier : identifiers())
        {
            // and get the node or shape point as a Node object
            var location = locationForIdentifier.get(identifier);
            var point = base.shapePointForLocation(location);
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
    public boolean contains(Location location)
    {
        return identifierForLocation.containsKey(location);
    }

    /**
     * Decrements the reference count to the given location, removing it if there are no more references
     */
    public void dereference(Location location)
    {
        referenceCount.dereference(location);
        if (!referenceCount.isReferenced(location))
        {
            var identifier = identifier(location);
            locationForIdentifier.remove(identifier);
            identifierForLocation.remove(location);
        }
    }

    /**
     * @return The OSM node identifier for the given location
     */
    public PbfNodeIdentifier identifier(Location location)
    {
        return identifierForLocation.get(location);
    }

    /**
     * @return The {@link PbfNodeIdentifier}s in this store in sorted order.
     */
    public Iterable<PbfNodeIdentifier> identifiers()
    {
        return locationForIdentifier.descendingKeySet();
    }

    /**
     * @return The location for the given identifier if it exists in this store
     */
    public Location location(PbfNodeIdentifier identifier)
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
    public PbfNodeIdentifier nextNodeIdentifier()
    {
        return new PbfNodeIdentifier(nextIdentifier--);
    }

    /**
     * @return The next available synthetic way identifier
     */
    public PbfWayIdentifier nextWayIdentifier()
    {
        return new PbfWayIdentifier(nextIdentifier--);
    }

    /**
     * Increments the reference count to the given location, adding the given location if it doesn't yet exist.
     */
    @SuppressWarnings("ConstantConditions")
    public void reference(Location location)
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
            PbfNodeIdentifier identifier = null;
            var point = base.shapePointForLocation(location);
            if (point != null)
            {
                identifier = (PbfNodeIdentifier) point.mapIdentifier();
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
        return Count.count(locationForIdentifier.size());
    }

    public PbfNodeStore subset(Set<PbfNodeIdentifier> include)
    {
        // Create subset
        var subset = new PbfNodeStore(base);

        // Go through the identifiers in this store
        for (var identifier : identifiers())
        {
            // and if the set of identifiers to include contains the given identifier
            if (include.contains(identifier))
            {
                // then copy the location, identifier and reference count to the subset
                var location = location(identifier);
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
        var lines = new StringList();

        // Go through each identifier
        for (var identifier : identifiers())
        {
            // Get location for identifier
            var location = location(identifier);

            // Get OSM tags for the location
            var tags = PbfTagList.EMPTY;
            var point = base.shapePointForLocation(location);
            if (point != null)
            {
                tags = point.tagList();
            }

            // Add open node tag
            lines.add("  <node id=\"" + identifier + "\" lat=\"" + location.latitude().asDegrees() + "\" lon=\""
                    + location.longitude().asDegrees() + "\" timestamp=\"" + new PbfTimestamp()
                    + "\" uid=\"2100001\" user=\"scout_osm\" version=\"1\"" + (tags.isEmpty() ? "/>" : ">"));

            // Add OSM tags
            for (var tag : tags)
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
