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

package com.telenav.mesakit.map.geography.projection.projections;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.projection.MetricCoordinate;
import com.telenav.mesakit.map.geography.projection.MetricProjection;

import static com.telenav.mesakit.map.measurements.geographic.Distance.EARTH_RADIUS_MAJOR;
import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.exp;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * @author jonathanl (shibo)
 * @see <a href="https://www.baeldung.com/java-convert-latitude-longitude">Mercator Projection in Java</a>
 */
public class SphericalMercatorMetricProjection implements MetricProjection
{
    private static final double EARTH_RADIUS_IN_METERS = EARTH_RADIUS_MAJOR.asMeters();

    @Override
    public MetricCoordinate toCoordinate(final Location location)
    {
        return new MetricCoordinate(
                longitudeInDegreesToMeters(location.longitudeInDegrees()),
                latitudeInDegreesToMeters(location.latitudeInDegrees()));
    }

    @Override
    public Location toLocation(final MetricCoordinate coordinate)
    {
        return Location.degrees(
                metersToLatitudeInDegrees(coordinate.yInMeters()),
                metersToLongitudeInDegrees(coordinate.xInMeters()));
    }

    private double latitudeInDegreesToMeters(final double latitudeInDegrees)
    {
        final var latitudeInRadians = toRadians(Latitude.inRange(latitudeInDegrees));
        return EARTH_RADIUS_IN_METERS * Math.log(Math.tan(Math.PI / 4 + latitudeInRadians / 2));
    }

    private double longitudeInDegreesToMeters(final double longitudeInDegrees)
    {
        return EARTH_RADIUS_IN_METERS * toRadians(Longitude.inRange(longitudeInDegrees));
    }

    private double metersToLatitudeInDegrees(final double yInMeters)
    {
        return Latitude.inRange(toDegrees(atan(exp(yInMeters / EARTH_RADIUS_IN_METERS)) * 2 - PI / 2));
    }

    private double metersToLongitudeInDegrees(final double xInMeters)
    {
        return Longitude.inRange(toDegrees(xInMeters / EARTH_RADIUS_IN_METERS));
    }
}
