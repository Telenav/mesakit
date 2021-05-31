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

import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;
import org.intellij.lang.annotations.Identifier;

public class TmcTableIdentifier extends Identifier
{
    private static final Logger LOGGER = com.telenav.kivakit.kernel.logging.LoggerFactory.newLogger();

    private static final TmcTableIdentifierExtractor extractor = new TmcTableIdentifierExtractor(LOGGER);

    private static final int TABLE_MASK = 100;

    public static TmcTableIdentifier fromTmcIdentifier(final RoadSectionIdentifier tmc)
    {
        return extractor.extract(tmc);
    }

    public TmcTableIdentifier(final int value)
    {
        super(value);
    }

    private TmcTableIdentifier()
    {
    }

    public int countryCode()
    {
        return asInteger() / TABLE_MASK;
    }

    public int tableNumber()
    {
        return asInteger() % TABLE_MASK;
    }
}
