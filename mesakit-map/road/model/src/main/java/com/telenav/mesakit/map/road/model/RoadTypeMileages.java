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

import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.kivakit.kernel.language.values.level.Percent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RoadTypeMileages
{
    private final Map<RoadType, Distance> roadTypeMileages = new HashMap<>();

    private Distance totalMileages = Distance.ZERO;

    public void add(final RoadType roadType, final Distance length)
    {
        final var mileage = roadTypeMileages.computeIfAbsent(roadType, k -> Distance.ZERO);
        roadTypeMileages.put(roadType, mileage.add(length));
        totalMileages = totalMileages.add(length);
    }

    public Distance mileage(final RoadType roadType)
    {
        var mileage = roadTypeMileages.get(roadType);
        if (mileage == null)
        {
            mileage = Distance.ZERO;
        }

        return mileage;
    }

    public Distance mileages()
    {
        return totalMileages;
    }

    public Percent roadTypMileagePercent(final RoadType roadType)
    {
        if (totalMileages.equals(Distance.ZERO))
        {
            return Percent._0;
        }
        else
        {
            final var mileage = mileage(roadType);
            return Percent.of(mileage.asMeters() * 100 / mileages().asMeters());
        }
    }

    public Iterable<RoadType> roadTypes()
    {
        return roadTypeMileages.keySet();
    }

    public List<Entry<RoadType, Distance>> sortedRoadTypeMileages()
    {
        final List<Entry<RoadType, Distance>> elements = new ArrayList<>(roadTypeMileages.entrySet());

        elements.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return elements;
    }

    @Override
    public String toString()
    {
        final var builder = new StringBuilder();
        for (final var roadTypeMileage : sortedRoadTypeMileages())
        {
            final var roadType = roadTypeMileage.getKey();
            final var mileages = roadTypeMileage.getValue();
            builder.append(roadType);
            builder.append("\t");
            builder.append(mileages);
            builder.append("\t");
            builder.append(Percent.of(mileages.asMiles() * 100.0 / mileages().asMiles()));
            builder.append("\n");
        }

        return builder.toString();
    }
}
