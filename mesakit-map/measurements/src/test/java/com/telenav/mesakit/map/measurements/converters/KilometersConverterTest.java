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

package com.telenav.mesakit.map.measurements.converters;

import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.project.MeasurementsUnitTest;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.listeners.ThrowingListener;
import org.junit.Test;

public class KilometersConverterTest extends MeasurementsUnitTest
{
    @Test
    public void testToObject()
    {
        var distance = new Distance.KilometersConverter(new ThrowingListener()).convert("0");
        ensureEqual(Distance.kilometers(0), distance);

        distance = new Distance.KilometersConverter(new ThrowingListener()).convert("33.3");
        ensureEqual(Distance.kilometers(33.3), distance);

        ensureNull(new Distance.KilometersConverter(Listener.none()).convert("hello"));
    }
}
