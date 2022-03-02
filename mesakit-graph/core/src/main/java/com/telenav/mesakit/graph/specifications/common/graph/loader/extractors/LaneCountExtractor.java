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

import com.telenav.kivakit.coredata.extraction.BaseExtractor;
import com.telenav.kivakit.core.language.strings.Strings;
import com.telenav.kivakit.language.count.Count;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;

public class LaneCountExtractor extends BaseExtractor<Count, PbfWay>
{
    private final String key;

    public LaneCountExtractor(Listener listener)
    {
        this(listener, "lanes");
    }

    public LaneCountExtractor(Listener listener, String key)
    {
        super(listener);
        this.key = key;
    }

    @Override
    public Count onExtract(PbfWay way)
    {
        var lanes = 1;
        var lanesString = way.tagValue(key);
        if (lanesString != null)
        {
            if (Strings.isNaturalNumber(lanesString))
            {
                lanes = Integer.parseInt(lanesString);
            }
            else
            {
                var total = 0;
                for (var laneCount : lanesString.split("\\s*[;+]\\s*"))
                {
                    if (Strings.isNaturalNumber(laneCount))
                    {
                        total += Integer.parseInt(laneCount);
                    }
                }
                if (total > 0)
                {
                    lanes = total;
                }
            }
        }
        return Count.count(lanes);
    }
}
