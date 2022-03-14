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

package com.telenav.mesakit.map.measurements.motion.speeds;

import com.telenav.kivakit.core.math.Average;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.measurements.lexakai.DiagramMapMeasurementMotion;

/**
 * Computes the average of a series of {@link Speed} samples.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementMotion.class)
@LexakaiJavadoc(complete = true)
public class AverageSpeed extends Average
{
    public void add(Speed speed)
    {
        super.add(speed.asMetersPerHour());
    }

    @UmlRelation(label = "computes")
    public Speed averageSpeed()
    {
        return Speed.metersPerHour(average());
    }
}
