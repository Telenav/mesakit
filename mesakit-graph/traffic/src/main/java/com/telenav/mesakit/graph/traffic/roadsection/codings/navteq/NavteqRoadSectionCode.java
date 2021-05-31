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

package com.telenav.mesakit.graph.traffic.roadsection.codings.navteq;

import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;

public class NavteqRoadSectionCode extends RoadSectionCode
{
    public static class Converter extends BaseStringConverter<NavteqRoadSectionCode>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected NavteqRoadSectionCode onConvertToObject(final String value)
        {
            return value == null || "".equals(value) ? null : new NavteqRoadSectionCode(Long.parseLong(value));
        }
    }

    private final long code;

    public NavteqRoadSectionCode(final long code)
    {
        this.code = code;
    }

    @Override
    public RoadSectionIdentifier asIdentifier(final boolean lookupDatabase)
    {
        return RoadSectionIdentifier.forCodingSystemAndIdentifier(codingSystem(), this.code, lookupDatabase);
    }

    @Override
    public String code()
    {
        return Long.toString(this.code);
    }

    @Override
    public final RoadSectionCodingSystem codingSystem()
    {
        return RoadSectionCodingSystem.NAVTEQ_EDGE_IDENTIFIER;
    }
}
