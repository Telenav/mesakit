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

package com.telenav.mesakit.map.geography.testing;

import com.telenav.kivakit.core.messaging.listeners.ThrowingListener;
import com.telenav.kivakit.primitive.collections.PrimitiveCollectionsKryoTypes;
import com.telenav.kivakit.serialization.kryo.types.CoreKryoTypes;
import com.telenav.kivakit.serialization.kryo.types.KryoTypes;
import com.telenav.mesakit.map.geography.GeographyKryoTypes;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.MeasurementsKryoTypes;
import com.telenav.mesakit.map.measurements.testing.MeasurementsUnitTest;

public class GeographyUnitTest extends MeasurementsUnitTest
{
    @Override
    protected KryoTypes kryoTypes()
    {
        return new CoreKryoTypes()
                .mergedWith(new MeasurementsKryoTypes())
                .mergedWith(new PrimitiveCollectionsKryoTypes())
                .mergedWith(new GeographyKryoTypes());
    }

    protected Location location(double latitude, double longitude)
    {
        return Location.degrees(latitude, longitude);
    }

    @Override
    protected GeographyRandomValueFactory newRandomValueFactory()
    {
        return new GeographyRandomValueFactory();
    }

    protected Rectangle rectangle(String rectangle)
    {
        return new Rectangle.Converter(new ThrowingListener()).convert(rectangle);
    }
}
