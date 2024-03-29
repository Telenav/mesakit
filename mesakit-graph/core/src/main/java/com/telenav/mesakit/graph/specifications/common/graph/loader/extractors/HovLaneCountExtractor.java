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

import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;

public class HovLaneCountExtractor extends BaseExtractor<Count, PbfWay>
{
    private final LaneCountExtractor laneCountExtractor;

    public HovLaneCountExtractor(Listener listener)
    {
        super(listener);
        laneCountExtractor = new LaneCountExtractor(listener);
    }

    @Override
    public Count onExtract(PbfWay way)
    {
        var hov = way.tagValue("hov");
        if (hov != null)
        {
            if ("lane".equals(hov) || "designated".equals(hov))
            {
                return Count._1;
            }
            return Count.parseCount(this, hov);
        }
        var lanes = way.tagValueAsCount("hov:lanes");
        if (lanes != null)
        {
            return lanes;
        }
        if (way.tagValueIs("hov", "access"))
        {
            return laneCountExtractor.extract(way);
        }
        return Count._0;
    }
}
