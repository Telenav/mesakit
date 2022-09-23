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

import com.telenav.kivakit.primitive.collections.Quantizable;

/**
 *
 */
public enum OverpassUnderpassType implements Quantizable
{
    NONE(0),
    OVERPASS(1),
    UNDERPASS(2);

    public static OverpassUnderpassType forIdentifier(Integer identifier)
    {
        for (var value : values())
        {
            if (value.identifier == identifier)
            {
                return value;
            }
        }
        return null;
    }

    private final int identifier;

    OverpassUnderpassType(int identifier)
    {
        this.identifier = identifier;
    }

    public int identifier()
    {
        return identifier;
    }

    @Override
    public long quantum()
    {
        return identifier;
    }
}
