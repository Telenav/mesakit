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

package com.telenav.tdk.graph.specifications.unidb.graph.node.model.attributes.extractor;

import com.telenav.tdk.core.data.extraction.BaseExtractor;
import com.telenav.tdk.core.kernel.language.primitive.Doubles;
import com.telenav.tdk.core.kernel.messaging.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfNode;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes.AdasZCoordinate;

public class AdasZCoordinateExtractor extends BaseExtractor<AdasZCoordinate, PbfNode>
{
    public AdasZCoordinateExtractor(final Listener<Message> listener)
    {
        super(listener);
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public AdasZCoordinate onExtract(final PbfNode node)
    {
        final var value = node.tagValue("adas:z_coord");
        if (value != null)
        {
            // Value always has two digits of precision
            final var meters = Doubles.fastParse(value, 100.0);
            if (meters != Doubles.INVALID)
            {
                final var coordinate = AdasZCoordinate.meters(meters);
                if (coordinate != null)
                {
                    return coordinate;
                }
            }
            warning("Invalid altitude of $ meters", value);
        }
        return null;
    }
}
