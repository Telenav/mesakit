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

import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.language.Objects;

public class Intersection
{
    public static Intersection forStreets(RoadName mainStreet, RoadName crossStreet)
    {
        if (mainStreet != null && crossStreet != null)
        {
            return new Intersection(mainStreet, crossStreet);
        }
        return null;
    }

    private final RoadName mainStreet;

    private final RoadName crossStreet;

    private Intersection(RoadName mainStreet, RoadName crossStreet)
    {
        this.mainStreet = mainStreet;
        this.crossStreet = crossStreet;
    }

    public RoadName crossStreet()
    {
        return crossStreet;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Intersection that)
        {
            return Objects.isEqual(mainStreet, that.mainStreet) && Objects.isEqual(crossStreet, that.crossStreet);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(mainStreet, crossStreet);
    }

    public RoadName mainStreet()
    {
        return mainStreet;
    }
}
