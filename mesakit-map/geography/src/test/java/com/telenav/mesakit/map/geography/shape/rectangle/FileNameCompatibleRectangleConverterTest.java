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

package com.telenav.mesakit.map.geography.shape.rectangle;

import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.test.UnitTest;
import com.telenav.mesakit.map.geography.Location;
import org.junit.Test;

public class FileNameCompatibleRectangleConverterTest extends UnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void extraction()
    {
        var group = Rectangle.FileNameConverter.group(LOGGER);
        var matcher = group.matcher("-45.0_-45.0_-45.0_-45.0");
        ensure(matcher.matches());
        ensureEqual(Rectangle.fromLocations(Location.degrees(-45, -45), Location.degrees(-45, -45)),
                group.get(matcher));
    }

    @Test
    public void matches()
    {
        var group = Rectangle.FileNameConverter.group(LOGGER);
        ensure(group.matches("-45.0_-45.0_-45.0_-45.0"));
    }

    @Test
    public void mismatches()
    {
        var group = Rectangle.FileNameConverter.group(LOGGER);
        ensureFalse(group.matches("xyz"));
        ensureFalse(group.matches("-45.0,-45.0:-45.0,-45.0"));
        ensureFalse(group.matches("-45.0,-45.0,-45.0,-45.0"));
    }
}
