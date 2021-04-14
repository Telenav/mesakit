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

package com.telenav.aonia.map.ui.swing.debug.debuggers.indexing.polygon;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.indexing.polygon.PolygonSpatialIndexDebugger;
import com.telenav.aonia.map.geography.shape.polyline.Polyline;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.geography.shape.segment.Segment;
import com.telenav.aonia.map.ui.swing.debug.View;
import com.telenav.aonia.map.ui.swing.debug.viewables.ViewableLocation;
import com.telenav.aonia.map.ui.swing.debug.viewables.ViewablePolyline;
import com.telenav.aonia.map.ui.swing.debug.viewables.ViewableRectangle;

import java.awt.Color;

/**
 * @author jonathanl (shibo)
 */
public class VisualPolygonSpatialIndexDebugger implements PolygonSpatialIndexDebugger
{
    private final View view;

    public VisualPolygonSpatialIndexDebugger(final View view)
    {
        this.view = view;
    }

    @Override
    public void leaf(final Rectangle bounds, final Segment a, final Segment b, final Location inside)
    {
        view(bounds, Color.LIGHT_GRAY, Color.RED, null);
        if (a != null)
        {
            view(a.asPolyline(), Color.LIGHT_GRAY, Color.BLUE, null);
        }
        if (b != null)
        {
            view(b.asPolyline(), Color.LIGHT_GRAY, Color.BLUE, null);
        }
        if (inside != null)
        {
            view.add(new ViewableLocation(inside, Color.LIGHT_GRAY, Color.GREEN, null));
        }
    }

    @Override
    public void quadrant(final Rectangle bounds)
    {
        view(bounds, Color.LIGHT_GRAY, Color.RED, null);
    }

    private void view(final Rectangle rectangle, final Color background, final Color foreground, final String label)
    {
        view.add(new ViewableRectangle(rectangle, background, foreground, label));
    }

    private void view(final Polyline polyline, final Color background, final Color foreground, final String label)
    {
        view.add(new ViewablePolyline(polyline, background, foreground, label));
    }
}
