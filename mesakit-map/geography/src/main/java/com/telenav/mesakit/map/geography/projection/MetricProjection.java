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

package com.telenav.mesakit.map.geography.projection;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;

/**
 * A projection between {@link Location}s (expressed in {@link Latitude} and {@link Longitude}) and metric coordinates
 * (expressed in meters on x and y axes with {@link MetricCoordinate}). The metric coordinate system, like the latitude
 * and longitude map coordinate system, has its origin at 0,0 in the center of the map. For projecting map coordinates
 * to a drawing surface, see mesakit-map-ui-desktop.
 *
 * @author jonathanl (shibo)
 */
public interface MetricProjection
{

    /**
     * @return The given location as a coordinate in meters from the map origin (0, 0)
     */
    MetricCoordinate toCoordinate(Location location);

    /**
     * @return The given coordinate in meters from the map origin (0, 0) as a location
     */
    Location toLocation(MetricCoordinate coordinate);
}
