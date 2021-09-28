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

package com.telenav.mesakit.map.geography.project;

import com.telenav.kivakit.kernel.messaging.listeners.ThrowingListener;
import com.telenav.kivakit.serialization.kryo.KryoTypes;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.GeographyProject;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.project.MeasurementsUnitTest;

public class GeographyUnitTest extends MeasurementsUnitTest
{
    @Override
    protected KryoTypes kryoTypes()
    {
        return GeographyProject.get().kryoTypes();
    }

    protected Location location(final double latitude, final double longitude)
    {
        return Location.degrees(latitude, longitude);
    }

    @Override
    protected GeographyRandomValueFactory randomValueFactory()
    {
        return newRandomValueFactory(GeographyRandomValueFactory::new);
    }

    protected Rectangle rectangle(final String rectangle)
    {
        return new Rectangle.Converter(new ThrowingListener()).convert(rectangle);
    }
}