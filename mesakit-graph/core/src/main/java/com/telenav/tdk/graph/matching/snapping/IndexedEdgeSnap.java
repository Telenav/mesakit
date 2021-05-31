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

package com.telenav.kivakit.graph.matching.snapping;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.map.geography.polyline.PolylineSnap;

public class IndexedEdgeSnap extends EdgeSnap
{
    private final int index;

    public IndexedEdgeSnap(final Edge edge, final PolylineSnap snap, final int index)
    {
        super(edge, snap);
        this.index = index;
    }

    public IndexedEdgeSnap(final EdgeSnap snap, final int index)
    {
        this(snap.edge(), snap, index);
    }

    public int index()
    {
        return index;
    }
}
