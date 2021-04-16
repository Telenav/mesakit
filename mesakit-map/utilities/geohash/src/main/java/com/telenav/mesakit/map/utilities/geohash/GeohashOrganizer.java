////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.utilities.geohash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A special collection for geohashes which provides:
 * <ul>
 * <li>easy addition of elements</li>
 * <li>automatic compacting of children in parents</li>
 * </ul>
 *
 * @author Mihai Chintoanu
 * @author jonathanl (shibo)
 */
class GeohashOrganizer
{
    private class Node
    {
        private final Geohash geohash;

        private final Collection<Node> children = new HashSet<>();

        public Node(final Geohash geohash)
        {
            this.geohash = geohash;
        }

        public void add(final Node node)
        {
            children.add(node);
        }

        public void compact()
        {
            for (final var child : children)
            {
                final var childGeohash = child.geohash();
                final var childLevel = levels.get(childGeohash.depth());
                if (!childLevel.contains(childGeohash))
                {
                    throw new IllegalStateException("No node found for geohash " + childGeohash);
                }
                else
                {
                    childLevel.remove(childGeohash);
                    child.compact();
                }
            }
            children.clear();
        }

        public Geohash geohash()
        {
            return geohash;
        }

        public boolean isLeaf()
        {
            return children.isEmpty();
        }

        public int numberOfMissingChildren()
        {
            var count = 0;
            for (final var child : children)
            {
                if (child.isLeaf())
                {
                    count++;
                }
            }
            return geohash.numberOfChildren() - count;
        }
    }

    private class TreeLevel
    {
        private final Map<Geohash, Node> nodeForGeohash = new HashMap<>();

        public Node add(final Geohash geohash)
        {
            final var node = new Node(geohash);
            nodeForGeohash.put(geohash, node);
            return node;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean contains(final Geohash geohash)
        {
            return nodeForGeohash.containsKey(geohash);
        }

        public Node get(final Geohash geohash)
        {
            return nodeForGeohash.get(geohash);
        }

        public Collection<Node> nodes()
        {
            return nodeForGeohash.values();
        }

        public void remove(final Geohash geohash)
        {
            nodeForGeohash.remove(geohash);
        }
    }

    private final List<TreeLevel> levels = new ArrayList<>();

    private final int interiorCompactingTolerance;

    private final int borderCompactingTolerance;

    /**
     * @see Geohasher
     */
    public GeohashOrganizer(final int interiorCompactingTolerance, final int borderCompactingTolerance)
    {
        if (borderCompactingTolerance < interiorCompactingTolerance)
        {
            throw new IllegalArgumentException(
                    "Border compacting tolerance can not be less than" + " interior compacting tolerance");
        }
        this.interiorCompactingTolerance = interiorCompactingTolerance;
        this.borderCompactingTolerance = borderCompactingTolerance;
    }

    protected GeohashOrganizer()
    {
        this(0, 0);
    }

    public void add(final Geohash geohash)
    {
        // Create any missing levels
        final var maximumDepth = levels.size();
        if (maximumDepth <= geohash.depth())
        {
            for (var i = maximumDepth; i <= geohash.depth(); i++)
            {
                levels.add(new TreeLevel());
            }
        }

        // Get the level for the geohash
        final var level = levels.get(geohash.depth());

        // If the map doesn't have this geohash
        if (!level.contains(geohash))
        {
            // then add a new node to the map
            final var node = level.add(geohash);

            // and add the new node to the tree, possibly creating nodes up to the root
            addToTree(node);
        }
        else
        {
            // the existing placeholder (interior) node becomes a concrete data node (leaf)
            level.get(geohash).compact();
        }
    }

    public Collection<Geohash> all()
    {
        compact();
        final Collection<Geohash> all = new HashSet<>();
        for (final var level : levels)
        {
            for (final var node : level.nodes())
            {
                if (node.isLeaf())
                {
                    all.add(node.geohash());
                }
            }
        }
        return all;
    }

    private void addToTree(final Node node)
    {
        final var parentGeohash = node.geohash().parent();
        final var parentLevel = levels.get(parentGeohash.depth());
        var parentNode = parentLevel.get(parentGeohash);
        if (parentNode == null)
        {
            parentNode = parentLevel.add(parentGeohash);
            if (!parentGeohash.isWorld())
            {
                addToTree(parentNode);
            }
        }
        parentNode.add(node);
    }

    private int borderLevelIndex()
    {
        return levels.size() - 1;
    }

    private void compact()
    {
        // The index of the level above the border level
        final var indexBeforeBorderLevel = borderLevelIndex() - 1;

        // Compact the children of all nodes above the border level
        for (var i = indexBeforeBorderLevel; i >= 0; i--)
        {
            final var tolerance = (i == indexBeforeBorderLevel) ? borderCompactingTolerance
                    : interiorCompactingTolerance;

            // Go through nodes at the given level
            for (final var node : levels.get(i).nodes())
            {
                // If the node has children, and it's not missing too many children
                if (!node.isLeaf() && node.numberOfMissingChildren() <= tolerance)
                {
                    // then compact its children
                    node.compact();
                }
            }
        }
    }
}
