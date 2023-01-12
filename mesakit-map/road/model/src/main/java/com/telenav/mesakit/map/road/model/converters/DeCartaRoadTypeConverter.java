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

package com.telenav.mesakit.map.road.model.converters;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.conversion.core.language.primitive.IntegerConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.road.model.DeCartaRoadType;

/**
 * @author Jianbo chen
 */
public class DeCartaRoadTypeConverter extends BaseStringConverter<DeCartaRoadType>
{
    private final IntegerConverter integerConverter;

    public DeCartaRoadTypeConverter(Listener listener)
    {
        super(listener, DeCartaRoadType.class);
        integerConverter = new IntegerConverter(listener);
    }

    @Override
    protected DeCartaRoadType onToValue(String value)
    {
        if ("null".equalsIgnoreCase(value))
        {
            return null;
        }
        var converted = integerConverter.convert(value);
        return converted == null ? null : DeCartaRoadType.forType(converted);
    }
}
