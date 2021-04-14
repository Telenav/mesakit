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
import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;

/**
 * A span from a start distance to an end distance, such as from 1 foot to 8 feet (the span is 7 feet). The length of
 * the span can be retrieved with {@link #length()}.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public class Span
{
    @UmlAggregation
    private final Distance start, end;

    public Span(final Distance start, final Distance end)
    {
        this.start = start;
        this.end = end;
        if (end.isLessThan(start))
        {
            throw new IllegalArgumentException("End distance " + end + " is smaller than start distance " + start);
        }
    }

    public Distance end()
    {
        return end;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Span)
        {
            final var that = (Span) object;
            return start.equals(that.start) && end.equals(that.end);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(start, end);
    }

    public Distance length()
    {
        return end.minus(start);
    }

    public Distance start()
    {
        return start;
    }
}
