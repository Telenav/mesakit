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

package com.telenav.kivakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfWay;
import com.telenav.kivakit.map.road.model.BridgeType;

import static com.telenav.kivakit.map.road.model.BridgeType.*;

public class BridgeTypeExtractor extends BaseExtractor<BridgeType, PbfWay>
{
    public BridgeTypeExtractor(final Listener<Message> listener)
    {
        super(listener);
    }

    @Override
    public BridgeType onExtract(final PbfWay way)
    {
        final var bridge = way.tagValue("bridge");
        if ("yes".equals(bridge) || "viaduct".equals(bridge))
        {
            return BRIDGE;
        }
        return way.tagValueIsYes("tunnel") ? TUNNEL : NONE;
    }
}
