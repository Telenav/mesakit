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

package com.telenav.aonia.map.measurements.motion.speeds;

import com.telenav.aonia.map.measurements.motion.Speed;
import com.telenav.aonia.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementMotion;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a class to calculate weighted average speed
 *
 * @author Junwei
 * @version 1.0.0 2013-3-26
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementMotion.class)
public class WeightedAverageSpeed
{
    private static class WeightedSpeed
    {
        private final double speedInMetersPerHour;

        private final double weight;

        public WeightedSpeed(final Speed speed, final double weight)
        {
            speedInMetersPerHour = speed.asMetersPerHour();
            this.weight = weight;
        }

        public double speedInMetersPerHour()
        {
            return speedInMetersPerHour;
        }

        public double weight()
        {
            return weight;
        }
    }

    private final List<WeightedSpeed> weightedSpeeds = new ArrayList<>();

    public void add(final Speed speed, final double weight)
    {
        if (speed != null)
        {
            weightedSpeeds.add(new WeightedSpeed(speed, weight));
        }
    }

    @UmlRelation(label = "computes")
    public Speed average()
    {
        if (weightedSpeeds.isEmpty())
        {
            return null;
        }

        var totalWeightedSpeed = 0D;
        var totalWeight = 0D;
        for (final var speed : weightedSpeeds)
        {
            totalWeightedSpeed += speed.speedInMetersPerHour() * speed.weight();
            totalWeight += speed.weight();
        }
        return Speed.metersPerHour(totalWeightedSpeed / totalWeight);
    }
}
