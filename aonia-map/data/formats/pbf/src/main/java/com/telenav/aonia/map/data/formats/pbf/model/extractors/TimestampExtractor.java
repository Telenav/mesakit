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

package com.telenav.aonia.map.data.formats.pbf.model.extractors;

import com.telenav.aonia.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.kivakit.core.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.core.kernel.language.time.Time;
import com.telenav.kivakit.core.kernel.messaging.Listener;

public class TimestampExtractor extends BaseExtractor<Time, PbfEntity<?>>
{
    public TimestampExtractor(final Listener listener)
    {
        super(listener);
    }

    @Override
    public Time onExtract(final PbfEntity<?> way)
    {
        final var time = way.timestamp().getTime();
        if (time > 0)
        {
            return Time.milliseconds(time);
        }
        return null;
    }
}
