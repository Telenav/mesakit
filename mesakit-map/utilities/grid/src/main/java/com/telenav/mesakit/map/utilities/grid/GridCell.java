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

package com.telenav.mesakit.map.utilities.grid;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

public class GridCell extends Rectangle
{
    private GridCellIdentifier identifier;

    private transient Integer hashCode;

    public GridCell(GridCellIdentifier identifier, Location bottomLeft, Location topRight)
    {
        super(bottomLeft, topRight);
        this.identifier = identifier;
    }

    public GridCell(GridCellIdentifier identifier, Rectangle that)
    {
        this(identifier, that.bottomLeft(), that.topRight());
    }

    private GridCell()
    {
    }

    /**
     * Grid cells are inclusive of their edges for containment. This means that a location exactly on the grid cell
     * border is included.
     */
    @Override
    public boolean contains(Location location)
    {
        return !containment(location).isOutside();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof GridCell)
        {
            var that = (GridCell) object;
            return this == that || identifier.equals(that.identifier);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (hashCode == null)
        {
            hashCode = identifier.hashCode();
        }
        return hashCode;
    }

    public final GridCellIdentifier identifier()
    {
        return identifier;
    }

    public String name()
    {
        return identifier().toString();
    }

    public String pureRectangleString()
    {
        return super.toString();
    }

    public String toRectangleString()
    {
        return super.toString();
    }

    @Override
    public String toString()
    {
        return "[GridCell identifier = " + identifier + ", bounds = " + super.toString() + "]";
    }
}
