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

package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.kernel.language.objects.Objects;

public class Intersection
{
    public static Intersection forStreets(final RoadName mainStreet, final RoadName crossStreet)
    {
        if (mainStreet != null && crossStreet != null)
        {
            return new Intersection(mainStreet, crossStreet);
        }
        return null;
    }

    private final RoadName mainStreet;

    private final RoadName crossStreet;

    private Intersection(final RoadName mainStreet, final RoadName crossStreet)
    {
        this.mainStreet = mainStreet;
        this.crossStreet = crossStreet;
    }

    public RoadName crossStreet()
    {
        return crossStreet;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Intersection)
        {
            final var that = (Intersection) object;
            return Objects.equal(mainStreet, that.mainStreet) && Objects.equal(crossStreet, that.crossStreet);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(mainStreet, crossStreet);
    }

    public RoadName mainStreet()
    {
        return mainStreet;
    }
}
