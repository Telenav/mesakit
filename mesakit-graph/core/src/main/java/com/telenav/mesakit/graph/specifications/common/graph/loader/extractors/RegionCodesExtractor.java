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

package com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.AdasRegionCode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;

public class RegionCodesExtractor extends BaseExtractor<ObjectList<AdasRegionCode>, PbfWay>
{
    public RegionCodesExtractor(final Listener listener)
    {
        super(listener);
    }

    @Override
    public ObjectList<AdasRegionCode> onExtract(final PbfWay way)
    {
        // Get adas region code like: +13952|-9344
        final var value = way.tagValue("adas:regional_code");
        if (null != value)
        {
            // split the value into parts on "|"
            final var parts = value.split("\\|");

            // then go through the parts, constructing region codes
            // adas:regional_code=>13952; adas:regional_code=>+13952|-9344
            final var regionCodes = new ObjectList<AdasRegionCode>(Maximum._1_000);
            for (final var part : parts)
            {
                if (part.startsWith("+"))
                {
                    regionCodes.add(0, new AdasRegionCode(Integer.parseInt(part)));
                }
                else
                {
                    regionCodes.add(new AdasRegionCode(-Integer.parseInt(part)));
                }
            }
            return regionCodes;
        }
        return null;
    }
}
