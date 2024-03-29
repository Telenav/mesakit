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

package com.telenav.mesakit.map.region.locale;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.State;

public class HascCode
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static HascCode parse(String code)
    {
        var parts = StringList.split(code, '-');
        if (parts.size() != 2)
        {
            LOGGER.warning("HASC code '" + code + "' not supported");
            return null;
        }

        Country country = null;
        var countryCode = RegionCode.parse(parts.get(0));
        if (countryCode != null)
        {
            country = Country.forRegionCode(countryCode);
        }
        if (country == null)
        {
            LOGGER.warning("HASC country code '" + parts.get(0) + "' of '" + code + "' is invalid");
        }

        State state = null;
        var stateCode = RegionCode.parse(parts.get(1));
        if (stateCode != null)
        {
            state = State.forRegionCode(stateCode);
        }
        if (state == null)
        {
            LOGGER.warning("HASC state code '" + parts.get(1) + "' of '" + code + "' is invalid");
        }

        return new HascCode(code, country, state);
    }

    private final String code;

    private final Country country;

    private final State state;

    private HascCode(String code, Country country, State state)
    {
        this.code = code;
        this.country = country;
        this.state = state;
    }

    public String code()
    {
        return code;
    }

    public Country country()
    {
        return country;
    }

    public State state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return code;
    }
}
