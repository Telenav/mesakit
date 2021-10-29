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
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;

public class UnderConstructionExtractor extends BaseExtractor<Boolean, PbfWay>
{
    public UnderConstructionExtractor(Listener listener)
    {
        super(listener);
    }

    @Override
    public Boolean onExtract(PbfWay way)
    {
        var highway = way.highway();
        if ("construction".equals(highway))
        {
            return Boolean.TRUE;
        }
        return way.tagValue("construction") != null;
    }
}
