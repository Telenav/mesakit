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

package com.telenav.mesakit.graph.traffic.roadsection.codings.tmc;

import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.test.UnitTest;
import com.telenav.kivakit.test.annotations.SlowTests;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({ SlowTests.class })
public class TmcTableIdentifierExtractorTest extends UnitTest
{
    private final TmcTableIdentifierExtractor extractor = new TmcTableIdentifierExtractor(Listener.none());

    @Test
    public void testExtract()
    {
        testExtract(105, "105+02345");
        testExtract(1209, "C09P02345");
        testExtract(1301, "D01-16358");
        testExtract(1426, "E26N43345");

        ensureNull(extractor.extract(RoadSectionIdentifier
                .forCodingSystemAndIdentifier(RoadSectionCodingSystem.OSM_EDGE_IDENTIFIER, 45678, false)));
    }

    private void testExtract(final int table, final String tmc)
    {
        final TmcCode tmcCode = TmcCode.forCode(tmc);
        final RoadSectionIdentifier tmcIdentifier = tmcCode.asIdentifier(false);
        ensureEqual(table, extractor.extract(tmcIdentifier).asInt());
    }
}
