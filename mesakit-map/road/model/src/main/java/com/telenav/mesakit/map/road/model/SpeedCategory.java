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

import com.telenav.kivakit.interfaces.numeric.Quantizable;
import com.telenav.kivakit.conversion.QuantizableConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.measurements.motion.Speed;

import java.util.HashMap;
import java.util.Map;

public class SpeedCategory implements Quantizable
{
    private static final Map<Integer, SpeedCategory> speedCategoryForIdentifier = new HashMap<>();

    static
    {
        new SpeedCategory(1, Speed.kilometersPerHour(190), Speed.kilometersPerHour(200));
        new SpeedCategory(2, Speed.kilometersPerHour(180), Speed.kilometersPerHour(190));
        new SpeedCategory(3, Speed.kilometersPerHour(170), Speed.kilometersPerHour(180));
        new SpeedCategory(4, Speed.kilometersPerHour(160), Speed.kilometersPerHour(170));
        new SpeedCategory(5, Speed.kilometersPerHour(150), Speed.kilometersPerHour(160));
        new SpeedCategory(6, Speed.kilometersPerHour(140), Speed.kilometersPerHour(150));
        new SpeedCategory(7, Speed.kilometersPerHour(130), Speed.kilometersPerHour(140));

        new SpeedCategory(8, Speed.kilometersPerHour(120), Speed.kilometersPerHour(130));
        new SpeedCategory(9, Speed.kilometersPerHour(110), Speed.kilometersPerHour(120));
        new SpeedCategory(10, Speed.kilometersPerHour(90), Speed.kilometersPerHour(110));
        new SpeedCategory(11, Speed.kilometersPerHour(80), Speed.kilometersPerHour(90));
        new SpeedCategory(12, Speed.kilometersPerHour(70), Speed.kilometersPerHour(80));
        new SpeedCategory(13, Speed.kilometersPerHour(60), Speed.kilometersPerHour(70));
        new SpeedCategory(14, Speed.kilometersPerHour(55), Speed.kilometersPerHour(60));
        new SpeedCategory(15, Speed.kilometersPerHour(50), Speed.kilometersPerHour(55));
        new SpeedCategory(16, Speed.kilometersPerHour(45), Speed.kilometersPerHour(50));
        new SpeedCategory(17, Speed.kilometersPerHour(40), Speed.kilometersPerHour(45));
        new SpeedCategory(18, Speed.kilometersPerHour(35), Speed.kilometersPerHour(40));
        new SpeedCategory(19, Speed.kilometersPerHour(30), Speed.kilometersPerHour(35));
        new SpeedCategory(20, Speed.kilometersPerHour(20), Speed.kilometersPerHour(30));
        new SpeedCategory(21, Speed.kilometersPerHour(10), Speed.kilometersPerHour(20));
        new SpeedCategory(22, Speed.kilometersPerHour(5), Speed.kilometersPerHour(10));
        new SpeedCategory(23, Speed.kilometersPerHour(0), Speed.kilometersPerHour(5));
    }

    public static SpeedCategory forIdentifier(int category)
    {
        return speedCategoryForIdentifier.get(category);
    }

    public static SpeedCategory forSpeed(Speed speed)
    {
        for (var category : speedCategoryForIdentifier.values())
        {
            if (category.includes(speed))
            {
                return category;
            }
        }
        return fastest();
    }

    public static class Converter extends QuantizableConverter<SpeedCategory>
    {
        public Converter(Listener listener)
        {
            super(listener, value -> forIdentifier(value.intValue()));
        }
    }

    private final int identifier;

    private final Speed maximum;

    private final Speed minimum;

    private SpeedCategory(int identifier, Speed minimum, Speed maximum)
    {
        this.identifier = identifier;
        this.minimum = minimum;
        this.maximum = maximum;
        speedCategoryForIdentifier.put(identifier, this);
    }

    public Speed average()
    {
        return Speed.milesPerHour(
                minimum.asMilesPerHour() + (maximum.asMilesPerHour() - minimum.asMilesPerHour()) / 2);
    }

    public int identifier()
    {
        return identifier;
    }

    public boolean includes(Speed speed)
    {
        return speed.isGreaterThanOrEqualTo(minimum) && speed.isLessThan(maximum);
    }

    public Speed maximum()
    {
        return maximum;
    }

    public Speed minimum()
    {
        return minimum;
    }

    @Override
    public long quantum()
    {
        return identifier;
    }

    public Speed speed()
    {
        return average();
    }

    @Override
    public String toString()
    {
        return String.format("%.1f to %.1f mph", minimum.asMilesPerHour(), maximum.asMilesPerHour());
    }

    private static SpeedCategory fastest()
    {
        return speedCategoryForIdentifier.get(1);
    }
}
