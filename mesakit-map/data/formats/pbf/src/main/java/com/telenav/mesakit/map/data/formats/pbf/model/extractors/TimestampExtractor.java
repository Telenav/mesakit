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

package com.telenav.mesakit.map.data.formats.pbf.model.extractors;

import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;

public class TimestampExtractor extends BaseExtractor<Time, PbfEntity<?>>
{
    public TimestampExtractor(Listener listener)
    {
        super(listener);
    }

    @Override
    public Time onExtract(PbfEntity<?> way)
    {
        var time = way.timestamp().getTime();
        if (time > 0)
        {
            return Time.epochMilliseconds(time);
        }
        return null;
    }
}
