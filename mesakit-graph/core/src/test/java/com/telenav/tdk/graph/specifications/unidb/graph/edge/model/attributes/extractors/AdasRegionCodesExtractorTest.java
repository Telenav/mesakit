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

package com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes.extractors;

import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.graph.project.KivaKitGraphCoreUnitTest;
import com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes.AdasRegionCode;
import org.junit.Test;

import java.util.List;

public class AdasRegionCodesExtractorTest extends KivaKitGraphCoreUnitTest
{
    @Test
    public void test()
    {
        final var tags = PbfTagList.create();
        tags.add("adas:regional_code", "+123|-456");
        final var way = new PbfWay(null).withTags(tags.asList());
        final List<AdasRegionCode> codes = new AdasRegionCodesExtractor(this).extract(way);
        final var expected = ObjectList.of(new AdasRegionCode(123), new AdasRegionCode(456));
        ensureEqual(codes, expected);
    }
}
