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

import com.telenav.kivakit.core.collections.map.BaseMap;

public class ShieldIcon
{
    private static final BaseMap<String, ShieldIcon> forIdentifier = new BaseMap<>(
            RoadLimits.SHIELD_ICONS)
    {
        @Override
        protected ShieldIcon onCreateValue(String identifier)
        {
            return new ShieldIcon(identifier);
        }
    };

    public static ShieldIcon forIdentifier(String identifier)
    {
        return forIdentifier.getOrCreate(identifier);
    }

    private final String identifier;

    private ShieldIcon(String identifier)
    {
        this.identifier = identifier;
    }

    public String identifier()
    {
        return identifier;
    }
}
