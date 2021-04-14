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

package com.telenav.aonia.map.utilities.grid;

import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.kivakit.core.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat;

public class GridCellIdentifier implements AsString
{
    private final int identifier;

    private final int longitudeIndex;

    private final int latitudeIndex;

    /**
     * For serialization purposes only
     */
    public GridCellIdentifier()
    {
        identifier = 0;
        longitudeIndex = 0;
        latitudeIndex = 0;
    }

    public GridCellIdentifier(final Grid grid, final int identifier)
    {
        this.identifier = identifier;
        latitudeIndex = Ints.high(identifier);
        longitudeIndex = Ints.low(identifier);
    }

    public GridCellIdentifier(final Grid grid, final int latitudeIndex, final int longitudeIndex)
    {
        identifier = Ints.forHighLow(latitudeIndex, longitudeIndex);
        this.longitudeIndex = longitudeIndex;
        this.latitudeIndex = latitudeIndex;
    }

    @Override
    public String asString(final StringFormat format)
    {
        return "[CellIdentifier " + toString() + "]";
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof GridCellIdentifier)
        {
            final var that = (GridCellIdentifier) object;
            return longitudeIndex == that.longitudeIndex && latitudeIndex == that.latitudeIndex;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(longitudeIndex, latitudeIndex);
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
