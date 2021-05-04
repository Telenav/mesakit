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

package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.desktop.graphics.drawing.Drawable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Stroke;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;

/**
 * @author jonathanl (shibo)
 */
public interface MapDrawable extends Drawable, Bounded
{
    Location location();

    @Override
    MapDrawable withColors(final Style style);

    @Override
    MapDrawable withDrawColor(final Color color);

    @Override
    MapDrawable withDrawStroke(final Stroke stroke);

    @Override
    MapDrawable withDrawStrokeWidth(final DrawingWidth width);

    @Override
    MapDrawable withFillColor(final Color color);

    @Override
    MapDrawable withFillStroke(final Stroke stroke);

    @Override
    MapDrawable withFillStrokeWidth(final DrawingWidth width);

    MapDrawable withLocation(final Location at);

    @Override
    MapDrawable withStyle(final Style style);

    @Override
    MapDrawable withTextColor(final Color color);
}
