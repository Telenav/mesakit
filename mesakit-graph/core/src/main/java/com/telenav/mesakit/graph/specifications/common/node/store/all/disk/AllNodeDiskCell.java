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

package com.telenav.mesakit.graph.specifications.common.node.store.all.disk;

import com.telenav.kivakit.core.language.Hash;
import com.telenav.mesakit.map.geography.Located;
import com.telenav.mesakit.map.geography.Location;

public class AllNodeDiskCell
{
    final int latitude;

    final int longitude;

    public AllNodeDiskCell(Located located)
    {
        this(located.location());
    }

    public AllNodeDiskCell(Location location)
    {
        latitude = (int) location.latitude().asDegrees();
        longitude = (int) location.longitude().asDegrees();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof AllNodeDiskCell)
        {
            var that = (AllNodeDiskCell) object;
            return latitude == that.latitude && longitude == that.longitude;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(latitude, longitude);
    }

    public String toFileString(String base)
    {
        return base + "." + latitude + "." + longitude;
    }
}
