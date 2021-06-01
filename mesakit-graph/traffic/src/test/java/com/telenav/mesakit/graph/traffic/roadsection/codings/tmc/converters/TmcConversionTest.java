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

package com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.converters;

import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode;
import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import org.junit.Test;

@SuppressWarnings("ConstantConditions")
public class TmcConversionTest extends MapGeographyUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final TmcCode.FromLongConverter longToTmcCode = new TmcCode.FromLongConverter(this);

    private final TmcCode.ToLongConverter tmcCodeToLong = new TmcCode.ToLongConverter(this);

    @Test
    public void test()
    {
        final var codes = new long[] { 101004098L, 105313016L, 1536308048L, 1230004329L, 1227304140L, 1102022844 };
        for (final long longIdentifier : codes)
        {
            final var tmcCode = this.longToTmcCode.convert(longIdentifier);
            final long newCode = this.tmcCodeToLong.convert(tmcCode);
            DEBUG.trace(longIdentifier + " => " + tmcCode + " => " + newCode);
            ensureEqual(newCode, longIdentifier);
        }
    }
}
