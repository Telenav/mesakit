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

package com.telenav.mesakit.map.geography.shape;

import com.telenav.mesakit.map.geography.Located;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramShape;

/**
 * Can tell you if a given location is contained within some shape outline
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramShape.class)
public interface Outline
{
    enum Containment
    {
        /** The location is inside the outline */
        INSIDE,

        /** The location is exactly on the outline */
        ON_BORDER,

        /** The location is outside the outline */
        OUTSIDE,

        /** The location's containment cannot be determined because of spatial index resolution */
        INDETERMINATE;

        public boolean isInside()
        {
            return !isOutside();
        }

        public boolean isOnBorder()
        {
            return this == ON_BORDER;
        }

        public boolean isOutside()
        {
            return this == OUTSIDE;
        }

        public boolean matches(final Containment that)
        {
            return isInside() == that.isInside() || this == INDETERMINATE || that == INDETERMINATE;
        }
    }

    /**
     * @param location The location
     * @return {@link Containment#INSIDE} if the location is inside this outline, {@link Containment#ON_BORDER} if it's
     * precisely on the border and {@link Containment#OUTSIDE} if it's outside the outline
     */
    Containment containment(Location location);

    default boolean contains(final Location location)
    {
        return containment(location).isInside();
    }

    default boolean contains(final Located located)
    {
        return contains(located.location());
    }
}
