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
import com.telenav.kivakit.core.collections.primitive.array.scalars.LongArray;
import com.telenav.kivakit.core.kernel.language.values.count.Estimate;

public class LocationArray
{
    private LongArray locations;

    public LocationArray(final String objectName, final Estimate estimate)
    {
        locations = new LongArray(objectName + ".locations");
        locations.initialSize(estimate);
        locations.initialize();
    }

    protected LocationArray()
    {
    }

    public Location get(final int index)
    {
        final var location = locations.get(index);
        if (location != Location.NULL)
        {
            return Location.fromLong(location);
        }
        return null;
    }

    public void set(final int index, final Location location)
    {
        locations.set(index, location == null ? Location.NULL : location.asLong());
    }
}
