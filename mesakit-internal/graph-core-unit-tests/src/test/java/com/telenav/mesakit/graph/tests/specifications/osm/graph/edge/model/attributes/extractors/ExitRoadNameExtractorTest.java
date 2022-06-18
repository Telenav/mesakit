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

package com.telenav.mesakit.graph.tests.specifications.osm.graph.edge.model.attributes.extractors;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.ExitRoadNameExtractor;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.region.testing.RegionUnitTest;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Set;

import static com.telenav.kivakit.core.messaging.Listener.emptyListener;
import static com.telenav.mesakit.map.road.name.standardizer.RoadNameStandardizer.Mode.MESAKIT_STANDARDIZATION;

public class ExitRoadNameExtractorTest extends RegionUnitTest
{
    @Test
    public void test()
    {
        ensureEqual(extract(way("exit_ref:eng", "Shibo Lane")), Set.of("Shibo Ln"));
        ensureEqual(extract(way("exit_ref:eng:trans:spa", "Camino de Shibo")), Set.of("Camino de Shibo"));
        ensureEqual(extract(way("exit_ref:eng", "Shibo Lane", "exit_ref:spa:trans:eng", "Shibo Road")), Set.of("Shibo Ln", "Shibo Rd"));
    }

    private Set<String> extract(PbfWay way)
    {
        var extractor = new ExitRoadNameExtractor(MapLocale.ENGLISH_UNITED_STATES.get(), MESAKIT_STANDARDIZATION, emptyListener());
        var roadNames = extractor.extract(way);
        return StringList.stringList(roadNames).asSet();
    }

    private PbfWay way(String... values)
    {
        var tags = new ArrayList<Tag>();
        for (int i = 0; i < values.length; i += 2)
        {
            tags.add(new Tag(values[i], values[i + 1]));
        }
        tags.add(new Tag("iso", "US"));
        return new PbfWay(null).withTags(tags);
    }
}
