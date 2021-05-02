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

package com.telenav.mesakit.map.ui.debug.debuggers;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.polygon.PolygonSpatialIndexDebugger;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.ui.desktop.theme.shapes.Bounds;
import com.telenav.mesakit.map.ui.desktop.theme.shapes.Locations;
import com.telenav.mesakit.map.ui.desktop.theme.shapes.Polylines;
import com.telenav.mesakit.map.ui.desktop.viewer.View;

/**
 * @author jonathanl (shibo)
 */
public class PolygonSpatialIndexVisualDebugger implements PolygonSpatialIndexDebugger
{
    private final View view;

    public PolygonSpatialIndexVisualDebugger(final View view)
    {
        this.view = view;
    }

    @Override
    public void leaf(final Rectangle bounds, final Segment a, final Segment b, final Location inside)
    {
        view.add(Bounds.ACTIVE_BOUNDING_BOX.withRectangle(bounds));
        if (a != null)
        {
            view.add(Polylines.NORMAL.withPolyline(a.asPolyline()));
        }
        if (b != null)
        {
            view.add(Polylines.NORMAL.withPolyline(b.asPolyline()));
        }
        if (inside != null)
        {
            view.add(Locations.LOCATION.withLocation(inside));
        }
    }

    @Override
    public void quadrant(final Rectangle bounds)
    {
        view.add(Bounds.INACTIVE_BOUNDING_BOX.withRectangle(bounds));
    }
}
