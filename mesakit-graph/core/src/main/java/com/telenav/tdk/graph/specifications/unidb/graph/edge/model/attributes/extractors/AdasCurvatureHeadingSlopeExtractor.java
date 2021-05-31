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

package com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes.extractors;

import com.telenav.kivakit.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.language.primitive.Ints;
import com.telenav.kivakit.kernel.language.string.StringList;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfWay;
import com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes.*;
import com.telenav.kivakit.map.measurements.Heading;

public class AdasCurvatureHeadingSlopeExtractor extends BaseExtractor<CurvatureHeadingSlopeSequence, PbfWay>
{
    public AdasCurvatureHeadingSlopeExtractor(final Listener<Message> listener)
    {
        super(listener);
    }

    @Override
    public CurvatureHeadingSlopeSequence onExtract(final PbfWay way)
    {
        // Get curvatures, headings and slopes (chs), if any
        final var value = way.tagValue("adas:chs");
        if (value != null)
        {
            // Parse <curvature>;<heading>;<slope> separated by "|", like:
            // "511;234;35|505;234;-17|504;234;-29"
            final var values = StringList.split(value, '|');

            final var size = values.size();
            final var curvatures = new long[size];
            final var headings = new long[size];
            final var slopes = new byte[size];

            for (var index = 0; index < size; index++)
            {
                final var parts = StringList.split(values.get(index), ';');
                if (parts.size() == 3)
                {
                    curvatures[index] = Ints.parse(parts.get(0), AdasCurvature.NULL);
                    headings[index] = Ints.parse(parts.get(1), Heading.NULL);
                    slopes[index] = (byte) ((Ints.parse(parts.get(2), CurvatureHeadingSlopeSequence.NULL_SLOPE) + 50) / 100);
                }
            }

            return new CurvatureHeadingSlopeSequence()
                    .curvatures(curvatures)
                    .headings(headings)
                    .slopes(slopes);
        }
        return null;
    }
}
