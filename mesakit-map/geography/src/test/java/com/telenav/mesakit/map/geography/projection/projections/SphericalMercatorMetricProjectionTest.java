/*
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * //
 * // © 2011-2021 Telenav, Inc.
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * // http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 * //
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 *
 */

package com.telenav.mesakit.map.geography.projection.projections;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.projection.MetricCoordinate;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Test;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

/**
 * @author jonathanl (shibo)
 */
public class SphericalMercatorMetricProjectionTest
{
    private final MetricCoordinate METRIC_ORIGIN = new MetricCoordinate(0, 0);

    private final MetricCoordinate METRIC_TOP_LEFT = new MetricCoordinate(-2.0037508342789244E7, 1.9971868880408563E7);

    private final MetricCoordinate METRIC_BOTTOM_LEFT = new MetricCoordinate(-2.0037508342789244E7, -1.997186888040857E7);

    private final MetricCoordinate METRIC_BOTTOM_RIGHT = new MetricCoordinate(2.0037508342789244E7, -1.997186888040857E7);

    private final MetricCoordinate METRIC_TOP_RIGHT = new MetricCoordinate(2.0037508342789244E7, 1.9971868880408563E7);

    private final MetricCoordinate METRIC_TELENAV_HEADQUARTERS = new MetricCoordinate(-1.3581489946437024E7, 4493033.643937797);

    @Test
    public void testToCoordinate()
    {
        final var projection = new SphericalMercatorMetricProjection();
        ensure(projection.toCoordinate(Location.ORIGIN).isCloseTo(METRIC_ORIGIN));
        ensure(projection.toCoordinate(Location.TOP_LEFT).isCloseTo(METRIC_TOP_LEFT));
        ensure(projection.toCoordinate(Location.BOTTOM_LEFT).isCloseTo(METRIC_BOTTOM_LEFT));
        ensure(projection.toCoordinate(Location.BOTTOM_RIGHT).isCloseTo(METRIC_BOTTOM_RIGHT));
        ensure(projection.toCoordinate(Location.TOP_RIGHT).isCloseTo(METRIC_TOP_RIGHT));
        ensure(projection.toCoordinate(Location.TELENAV_HEADQUARTERS).isCloseTo(METRIC_TELENAV_HEADQUARTERS));
    }

    @Test
    public void testToLocation()
    {
        final var projection = new SphericalMercatorMetricProjection();
        ensure(projection.toLocation(METRIC_ORIGIN).isClose(Location.ORIGIN, Distance.meters(1)));
        ensure(projection.toLocation(METRIC_TOP_LEFT).isClose(Location.TOP_LEFT, Distance.meters(1)));
        ensure(projection.toLocation(METRIC_BOTTOM_LEFT).isClose(Location.BOTTOM_LEFT, Distance.meters(1)));
        ensure(projection.toLocation(METRIC_BOTTOM_RIGHT).isClose(Location.BOTTOM_RIGHT, Distance.meters(1)));
        ensure(projection.toLocation(METRIC_TOP_RIGHT).isClose(Location.TOP_RIGHT, Distance.meters(1)));
        ensure(projection.toLocation(METRIC_TELENAV_HEADQUARTERS).isClose(Location.TELENAV_HEADQUARTERS, Distance.meters(1)));
    }
}
