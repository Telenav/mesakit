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

package com.telenav.aonia.map.measurements.geographic;

import com.telenav.aonia.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementGeographic;
import com.telenav.kivakit.math.statistics.Average;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

/**
 * Computes an average {@link Distance} given a series of samples.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public class AverageDistance extends Average
{
    public void add(final Distance distance)
    {
        super.add(distance.asMillimeters());
    }

    @UmlRelation(label = "computes")
    public Distance averageDistance()
    {
        return Distance.millimeters((long) average());
    }
}
