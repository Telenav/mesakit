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

package com.telenav.aonia.map.road.model.converters;

import com.telenav.aonia.map.road.model.DeCartaRoadType;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.data.conversion.string.primitive.IntegerConverter;
import com.telenav.kivakit.core.kernel.messaging.Listener;

/**
 * @author Jianbo chen
 */
public class DeCartaRoadTypeConverter extends BaseStringConverter<DeCartaRoadType>
{
    private final IntegerConverter integerConverter;

    public DeCartaRoadTypeConverter(final Listener listener)
    {
        super(listener);
        integerConverter = new IntegerConverter(listener);
    }

    @Override
    protected DeCartaRoadType onConvertToObject(final String value)
    {
        if ("null".equalsIgnoreCase(value))
        {
            return null;
        }
        final var converted = integerConverter.convert(value);
        return converted == null ? null : DeCartaRoadType.forType(converted);
    }
}
