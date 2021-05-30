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


package com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes.extractors;

import com.telenav.tdk.core.data.extraction.BaseExtractor;
import com.telenav.tdk.core.kernel.language.collections.list.ObjectList;
import com.telenav.tdk.core.kernel.messaging.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfWay;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes.AdasRegionCode;

/**
 *
 */
public class AdasRegionCodesExtractor extends BaseExtractor<ObjectList<AdasRegionCode>, PbfWay>
{
    public AdasRegionCodesExtractor(final Listener<Message> listener)
    {
        super(listener);
    }

    @Override
    public ObjectList<AdasRegionCode> onExtract(final PbfWay way)
    {
        final var tag = way.tagValue("adas:regional_code"); // +13952|-9344
        if (null != tag)
        {
            final var codes = new ObjectList<AdasRegionCode>();
            if (tag.contains("|"))
            {
                final var parts = tag.split("\\|");
                // adas:regional_code=>13952; adas:regional_code=>+13952|-9344
                for (final var part : parts)
                {
                    if (part.startsWith("+"))
                    {
                        codes.add(0, new AdasRegionCode(Integer.parseInt(part)));
                    }
                    else
                    {
                        codes.add(new AdasRegionCode(-Integer.parseInt(part)));
                    }
                }
            }
            else
            {
                codes.add(new AdasRegionCode(Integer.parseInt(tag)));
            }
            return codes;
        }
        return null;
    }
}
