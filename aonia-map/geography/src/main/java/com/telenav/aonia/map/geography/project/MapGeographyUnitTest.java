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

package com.telenav.aonia.map.geography.project;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.measurements.project.MapMeasurementsUnitTest;
import com.telenav.kivakit.core.kernel.messaging.listeners.ThrowingListener;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;

public class MapGeographyUnitTest extends MapMeasurementsUnitTest
{
    @Override
    protected KryoTypes kryoTypes()
    {
        return MapGeographyProject.get().kryoTypes();
    }

    protected Location location(final double latitude, final double longitude)
    {
        return Location.degrees(latitude, longitude);
    }

    @Override
    protected MapGeographyRandomValueFactory randomValueFactory()
    {
        return newRandomValueFactory(MapGeographyRandomValueFactory::new);
    }

    protected Rectangle rectangle(final String rectangle)
    {
        return new Rectangle.Converter(new ThrowingListener()).convert(rectangle);
    }
}
