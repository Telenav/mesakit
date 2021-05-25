////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.processing;

import com.telenav.kivakit.test.UnitTest;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagPatternFilter;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

public class PbfTagPatternFilterTest extends UnitTest
{
    @Test
    public void test()
    {
        // filter out adas and phonetics tags
        final PbfTagPatternFilter filter = new PbfTagPatternFilter("^(?!.*(adas:|phonetics)).*$");

        ensureFalse(filter.accepts(new Tag("adas:chs", "")));
        ensureFalse(filter.accepts(new Tag("adas:route_type", "")));
        ensureFalse(filter.accepts(new Tag("adas:complex_intersection", "")));
        ensureFalse(filter.accepts(new Tag("name:eng:phonetics_1:nas:m:street_name", "")));
        ensureFalse(filter.accepts(new Tag("name_1:eng:phonetics:eng:s:street_name", "")));
        ensureFalse(filter.accepts(new Tag("alt_name:eng:phonetics_8:nas:m:base_name", "")));

        ensure(filter.accepts(new Tag("rt", "")));
        ensure(filter.accepts(new Tag("fc", "")));
        ensure(filter.accepts(new Tag("tmc_ids", "")));
        ensure(filter.accepts(new Tag("rst", "")));
    }
}
