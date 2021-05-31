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

import com.telenav.kivakit.data.formats.pbf.model.tags.PbfWay;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.standardizer.RoadNameStandardizer;

import java.util.ArrayList;
import java.util.List;

public class OfficialRoadNameExtractor extends BaseRoadNameExtractor
{
    private static final String[] KEYS = new String[]
            {
                    "name",
                    "name_1",
                    "name_2"
            };

    public OfficialRoadNameExtractor(final MapLocale locale, final RoadNameStandardizer.Mode mode,
                                     final Listener listener)
    {
        super(locale, mode, listener);
    }

    @Override
    public List<RoadName> onExtract(final PbfWay way)
    {
        final List<RoadName> names = new ArrayList<>();

        addRoadName(names, way.tagValue("name"));
        addRoadName(names, way.tagValue("official_name"));
        addRoadName(names, way.tagValue("int_name"));
        addRoadName(names, way.tagValue("name:en"));
        addRoadName(names, way.tagValue("nat_name"));
        addRoadName(names, way.tagValue("name_1"));

        if (names.isEmpty())
        {
            addRoadName(names, way.tagValue("tiger:name_base"));
        }

        addRoadNameTranslations(names, way, KEYS);

        return names;
    }
}
