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

import com.telenav.kivakit.core.collections.map.StringMap;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Named @link{RoadTypeMileages}
 *
 * @author songg
 */
public class NamedRoadTypeMileages
{
    private final StringMap<RoadTypeMileages> namedRoadTypeMileages = new StringMap<>();

    private Distance mileages = Distance.ZERO;

    public void add(String name, RoadType roadType, Distance mileage)
    {
        var roadTypeMileages = namedRoadTypeMileages.get(name);
        if (roadTypeMileages == null)
        {
            roadTypeMileages = new RoadTypeMileages();
            namedRoadTypeMileages.put(name, roadTypeMileages);
        }

        roadTypeMileages.add(roadType, mileage);

        mileages = mileages.add(mileage);
    }

    public RoadTypeMileages get(String name)
    {
        return namedRoadTypeMileages.get(name);
    }

    public Distance mileages()
    {
        return mileages;
    }

    public Distance namedMileages(String name)
    {
        var roadTypeMileages = get(name);
        if (roadTypeMileages == null)
        {
            return Distance.ZERO;
        }

        return roadTypeMileages.mileages();
    }

    public Iterable<String> names()
    {
        return namedRoadTypeMileages.keySet();
    }

    public List<Entry<String, RoadTypeMileages>> sortedNamedRoadTypeMileages()
    {
        List<Entry<String, RoadTypeMileages>> elements = new ArrayList<>(namedRoadTypeMileages.entrySet());

        elements.sort((o1, o2) -> o2.getValue().mileages().compareTo(o1.getValue().mileages()));

        return elements;
    }
}
