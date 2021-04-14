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

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.indexing.polygon.PolygonSpatialIndex.Visitor;
import com.telenav.aonia.map.geography.shape.Outline.Containment;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;

/**
 * A quadrant is either an {@link Node} or a {@link Leaf}. It has a bounding rectangle and the specific subclass can
 * determine if a given point is inside the polygon or not.
 *
 * @author jonathanl (shibo)
 */
abstract class Quadrant
{
    /**
     * @return True if the location is inside the polygon as indexed by this quadrant
     */
    public abstract Containment contains(final Location location, Rectangle bounds);

    /**
     * Subclasses can call the debugger with the given bounds and their own information
     */
    public abstract void debug(final PolygonSpatialIndexDebugger debugger, final Rectangle bounds);

    /**
     * @param visitor The visitor that should visit this quadrant
     */
    protected abstract void visit(final Visitor visitor, Rectangle bounds);
}
