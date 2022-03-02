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

package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.collections.map.BaseMap;

/**
 * An exit number is a name rather than a number because of exits like "21a".
 */
public class ExitNumber
{
    private static final BaseMap<String, ExitNumber> forIdentifier = new BaseMap<>(
            RoadLimits.EXIT_NUMBERS)
    {
        @Override
        protected ExitNumber onInitialize(String identifier)
        {
            return new ExitNumber(identifier);
        }
    };

    public static ExitNumber forIdentifier(String identifier)
    {
        return forIdentifier.getOrCreate(identifier);
    }

    private final String identifier;

    private ExitNumber(String identifier)
    {
        this.identifier = identifier;
    }

    public String identifier()
    {
        return identifier;
    }
}
