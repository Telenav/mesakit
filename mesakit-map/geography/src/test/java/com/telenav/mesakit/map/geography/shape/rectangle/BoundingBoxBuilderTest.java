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

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.GeographyUnitTest;
import org.junit.Test;

public class BoundingBoxBuilderTest extends GeographyUnitTest
{
    @Test
    public void testAdd_LatitudeAndLongitude()
    {
        int bottom = Integer.MAX_VALUE;
        int left = Integer.MAX_VALUE;
        int top = Integer.MIN_VALUE;
        int right = Integer.MIN_VALUE;
        var boundingBox = new BoundingBoxBuilder();
        for (var i = 0; i < 100; i++)
        {
            Location location = newRandomValueFactory().newLocation();
            boundingBox.add(location.latitude().asDegrees(), location.longitude().asDegrees());
            bottom = Math.min(bottom, location.latitudeInDm7());
            left = Math.min(left, location.longitudeInDm7());
            top = Math.max(top, location.latitudeInDm7());
            right = Math.max(right, location.longitudeInDm7());
            Rectangle bounds = boundingBox.build();
            ensure(bounds != null);
            assert bounds != null;
            close(bottom, bounds.bottomLeft().latitudeInDm7());
            close(left, bounds.bottomLeft().longitudeInDm7());
            close(top, bounds.topRight().latitudeInDm7());
            close(right, bounds.topRight().longitudeInDm7());
        }
    }

    @Test
    public void testAdd_Location()
    {
        int bottom = Integer.MAX_VALUE;
        int left = Integer.MAX_VALUE;
        int top = Integer.MIN_VALUE;
        int right = Integer.MIN_VALUE;
        var boundingBox = new BoundingBoxBuilder();
        for (var i = 0; i < 100; i++)
        {
            Location location = newRandomValueFactory().newLocation();
            boundingBox.add(location);
            bottom = Math.min(bottom, location.latitudeInDm7());
            left = Math.min(left, location.longitudeInDm7());
            top = Math.max(top, location.latitudeInDm7());
            right = Math.max(right, location.longitudeInDm7());
            Rectangle bounds = boundingBox.build();
            ensure(bounds != null);
            assert bounds != null;
            close(bottom, bounds.bottomLeft().latitudeInDm7());
            close(left, bounds.bottomLeft().longitudeInDm7());
            close(top, bounds.topRight().latitudeInDm7());
            close(right, bounds.topRight().longitudeInDm7());
        }
    }

    private void close(int a, int b)
    {
        ensure(Math.abs(a - b) <= 1);
    }
}
