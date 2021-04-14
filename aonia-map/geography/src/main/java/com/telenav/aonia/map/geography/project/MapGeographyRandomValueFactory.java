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

import com.telenav.aonia.map.geography.Latitude;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.Longitude;
import com.telenav.aonia.map.geography.shape.rectangle.Height;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.geography.shape.rectangle.Size;
import com.telenav.aonia.map.geography.shape.rectangle.Width;
import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.measurements.project.MapMeasurementsRandomValueFactory;

public class MapGeographyRandomValueFactory extends MapMeasurementsRandomValueFactory
{
    private final Latitude MINIMUM_LATITUDE = Latitude.MINIMUM.plus(Angle.degrees(1));

    private final Latitude MAXIMUM_LATITUDE = Latitude.MAXIMUM.minus(Angle.degrees(1));

    public MapGeographyRandomValueFactory()
    {
    }

    public MapGeographyRandomValueFactory(final long seed)
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
    public Latitude newLatitude(final Latitude min, final Latitude max)
    {
        final var minimum = min.maximum(MINIMUM_LATITUDE).asMicrodegrees();
        final var maximum = max.minimum(MAXIMUM_LATITUDE).asMicrodegrees();
        final var difference = maximum - minimum;
        final var offset = random.nextInt(difference);
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
    public Location newLocation(final Rectangle bounds)
    {
        final var latitude = newLatitude(bounds.bottom(), bounds.top());
        final var longitude = newLongitude(bounds.left(), bounds.right());
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
    public Longitude newLongitude(final Longitude min, final Longitude max)
    {
        final var minimum = min.asMicrodegrees();
        final var maximum = max.asMicrodegrees();
        final var difference = maximum - minimum;
        final var offset = random.nextInt(difference);
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
    public Rectangle newRectangle(final Rectangle bounds)
    {
        return Rectangle.fromLocations(newLocation(bounds), newLocation(bounds));
    }

    public Rectangle newRectangle(final Rectangle bounds, final Distance maximumRadius)
    {
        return Rectangle.fromCenterAndRadius(newLocation(bounds), newDistance(Distance.MINIMUM, maximumRadius));
    }

    public Size newSize(final Size maximum)
    {
        return newSize(Size.ZERO, maximum);
    }

    public Size newSize(final Size min, final Size max)
    {
        final var minimumHeight = min.height().asMicrodegrees();
        final var maximumHeight = max.height().asMicrodegrees();
        final var minimumWidth = min.width().asMicrodegrees();
        final var maximumWidth = max.width().asMicrodegrees();

        final var heightDifference = maximumHeight - minimumHeight;
        final var widthDifference = maximumWidth - minimumWidth;

        final var heightOffset = random.nextInt(heightDifference);
        final var widthOffset = random.nextInt(widthDifference);

        return new Size(Width.microdegrees(minimumWidth + widthOffset),
                Height.microdegrees(minimumHeight + heightOffset));
    }
}
