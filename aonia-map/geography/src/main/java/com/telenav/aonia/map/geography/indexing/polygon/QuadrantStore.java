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

package com.telenav.aonia.map.geography.indexing.polygon;

import com.telenav.aonia.map.geography.shape.segment.Segment;
import com.telenav.kivakit.core.collections.primitive.array.scalars.IntArray;
import com.telenav.kivakit.core.kernel.language.values.count.Estimate;

public class QuadrantStore
{
    private int nodeIndex = 1;

    private int leafIndex = 1;

    private LocationArray leafAStart;

    private LocationArray leafAEnd;

    private LocationArray leafBStart;

    private LocationArray leafBEnd;

    private LocationArray leafInside;

    private final IntArray nodeNorthEast = new IntArray("NE");

    private final IntArray nodeNorthWest = new IntArray("NW");

    private final IntArray nodeSouthEast = new IntArray("SE");

    private final IntArray nodeSouthWest = new IntArray("SW");

    private PolygonSpatialIndex spatialIndex;

    public QuadrantStore(final PolygonSpatialIndex spatialIndex)
    {
        this.spatialIndex = spatialIndex;

        nodeNorthEast.initialSize(64);
        nodeNorthEast.initialize();

        nodeNorthWest.initialSize(64);
        nodeNorthWest.initialize();

        nodeSouthEast.initialSize(64);
        nodeSouthEast.initialize();

        nodeSouthWest.initialSize(64);
        nodeSouthWest.initialize();

        leafAStart = newLocationArray();
        leafAEnd = newLocationArray();
        leafBStart = newLocationArray();
        leafBEnd = newLocationArray();
        leafInside = newLocationArray();
    }

    protected QuadrantStore()
    {
    }

    public int add(final Node node)
    {
        // Save indexes
        nodeNorthEast.set(nodeIndex, node.northEastIndex());
        nodeNorthWest.set(nodeIndex, node.northWestIndex());
        nodeSouthEast.set(nodeIndex, node.southEastIndex());
        nodeSouthWest.set(nodeIndex, node.southWestIndex());

        // Next index
        return nodeIndex++;
    }

    int add(final Leaf leaf)
    {
        // Save a
        final var a = leaf.a();
        leafAStart.set(leafIndex, a == null ? null : a.start());
        leafAEnd.set(leafIndex, a == null ? null : a.end());

        // Save b
        final var b = leaf.b();
        leafBStart.set(leafIndex, b == null ? null : b.start());
        leafBEnd.set(leafIndex, b == null ? null : b.end());

        // Save inside
        final var inside = leaf.inside();
        leafInside.set(leafIndex, inside);

        // Next index
        final var negativeIndex = -leafIndex;
        leafIndex++;

        // Return negative index to indicate this is a leaf
        return negativeIndex;
    }

    Quadrant get(final int index)
    {
        // If this is a leaf,
        if (isLeafIndex(index))
        {
            // Convert to array index
            final var leafIndex = -index;

            // get a
            final var aStart = leafAStart.get(leafIndex);
            final var aEnd = leafAEnd.get(leafIndex);
            final var a = aStart == null || aEnd == null ? null : new Segment(aStart, aEnd);

            // get b
            final var bStart = leafBStart.get(leafIndex);
            final var bEnd = leafBEnd.get(leafIndex);
            final var b = bStart == null || bEnd == null ? null : new Segment(bStart, bEnd);

            // get inside
            final var inside = leafInside.get(leafIndex);

            // and return leaf
            return new Leaf(a, b, inside);
        }
        else
        {
            // otherwise get child indexes
            final var northEastIndex = nodeNorthEast.get(index);
            final var northWestIndex = nodeNorthWest.get(index);
            final var southEastIndex = nodeSouthEast.get(index);
            final var southWestIndex = nodeSouthWest.get(index);

            // and return node
            return new Node(spatialIndex, northEastIndex, northWestIndex, southEastIndex, southWestIndex);
        }
    }

    private boolean isLeafIndex(final int index)
    {
        return index < 0;
    }

    private LocationArray newLocationArray()
    {
        return new LocationArray("locations", Estimate._64);
    }
}
