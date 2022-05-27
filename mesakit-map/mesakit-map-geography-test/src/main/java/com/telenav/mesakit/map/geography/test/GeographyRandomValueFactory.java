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

package com.telenav.mesakit.map.geography.test;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.test.MeasurementsRandomValueFactory;

public class GeographyRandomValueFactory extends MeasurementsRandomValueFactory
{
    private final Latitude MINIMUM_LATITUDE = Latitude.MINIMUM.plus(Angle.degrees(1));

    private final Latitude MAXIMUM_LATITUDE = Latitude.MAXIMUM.minus(Angle.degrees(1));

    public GeographyRandomValueFactory()
    {
    }

    public GeographyRandomValueFactory(long seed)
    {
        super(seed);
    }

    public Latitude newLatitude()
    {
        return newLatitude(MINIMUM_LATITUDE, MAXIMUM_LATITUDE);
    }

    /**
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return A latitude value greater than or equal to the minimum and less than the maximum.
     */
    public Latitude newLatitude(Latitude min, Latitude max)
    {
        var minimum = min.maximum(MINIMUM_LATITUDE).asMicrodegrees();
        var maximum = max.minimum(MAXIMUM_LATITUDE).asMicrodegrees();
        var difference = maximum - minimum;
        var offset = random.nextInt(difference);
        return Latitude.microdegrees(minimum + offset);
    }

    public Location newLocation()
    {
        return newLocation(Rectangle.MAXIMUM);
    }

    /**
     * @param bounds The bounding region that should contain the location
     * @return A location within the bounding region
     */
    public Location newLocation(Rectangle bounds)
    {
        var latitude = newLatitude(bounds.bottom(), bounds.top());
        var longitude = newLongitude(bounds.left(), bounds.right());
        return new Location(latitude, longitude);
    }

    public Longitude newLongitude()
    {
        return newLongitude(Longitude.MINIMUM, Longitude.MAXIMUM);
    }

    /**
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return A longitude value greater than or equal to the minimum and less than the maximum.
     */
    public Longitude newLongitude(Longitude min, Longitude max)
    {
        var minimum = min.asMicrodegrees();
        var maximum = max.asMicrodegrees();
        var difference = maximum - minimum;
        var offset = random.nextInt(difference);
        return Longitude.microdegrees(minimum + offset);
    }

    public Rectangle newRectangle()
    {
        return newRectangle(Rectangle.MAXIMUM);
    }

    /**
     * @param bounds The boundary within which to create a new rectangle
     * @return A new rectangle contained within the specified bounds.
     */
    public Rectangle newRectangle(Rectangle bounds)
    {
        return Rectangle.fromLocations(newLocation(bounds), newLocation(bounds));
    }

    public Rectangle newRectangle(Rectangle bounds, Distance maximumRadius)
    {
        return Rectangle.fromCenterAndRadius(newLocation(bounds), newDistance(Distance.MINIMUM, maximumRadius));
    }

    public Size newSize(Size maximum)
    {
        return newSize(Size.ZERO, maximum);
    }

    public Size newSize(Size min, Size max)
    {
        var minimumHeight = min.height().asMicrodegrees();
        var maximumHeight = max.height().asMicrodegrees();
        var minimumWidth = min.width().asMicrodegrees();
        var maximumWidth = max.width().asMicrodegrees();

        var heightDifference = maximumHeight - minimumHeight;
        var widthDifference = maximumWidth - minimumWidth;

        var heightOffset = random.nextInt(heightDifference);
        var widthOffset = random.nextInt(widthDifference);

        return new Size(Width.microdegrees(minimumWidth + widthOffset),
                Height.microdegrees(minimumHeight + heightOffset));
    }
}
