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

import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import org.jetbrains.annotations.NotNull;

public class GridCellIdentifier implements StringFormattable
{
    private final int identifier;

    private final int latitudeIndex;

    private final int longitudeIndex;

    /**
     * For serialization purposes only
     */
    public GridCellIdentifier()
    {
        identifier = 0;
        longitudeIndex = 0;
        latitudeIndex = 0;
    }

    public GridCellIdentifier(Grid grid, int identifier)
    {
        this.identifier = identifier;
        latitudeIndex = Ints.intHighWord(identifier);
        longitudeIndex = Ints.intLowWord(identifier);
    }

    public GridCellIdentifier(Grid grid, int latitudeIndex, int longitudeIndex)
    {
        identifier = Ints.intForWords(latitudeIndex, longitudeIndex);
        this.longitudeIndex = longitudeIndex;
        this.latitudeIndex = latitudeIndex;
    }

    @Override
    public String asString(@NotNull Format format)
    {
        return "[CellIdentifier " + this + "]";
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof GridCellIdentifier that)
        {
            return longitudeIndex == that.longitudeIndex && latitudeIndex == that.latitudeIndex;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(longitudeIndex, latitudeIndex);
    }

    public int identifier()
    {
        return identifier;
    }

    public int latitudeIndex()
    {
        return latitudeIndex;
    }

    public int longitudeIndex()
    {
        return longitudeIndex;
    }

    @Override
    public final String toString()
    {
        return latitudeIndex + "-" + longitudeIndex;
    }
}
