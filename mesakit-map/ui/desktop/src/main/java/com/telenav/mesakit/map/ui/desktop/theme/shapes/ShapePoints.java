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

package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.NIGHT_SHIFT;
import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.GRAYED;

/**
 * @author jonathanl (shibo)
 */
public class ShapePoints
{
    public static final MapDot NORMAL = MapDot.dot()
            .withStyle(GRAYED)
            .withRadius(meters(1))
            .withDrawStrokeWidth(meters(0.2));

    public static final MapDot SELECTED = MapDot.dot()
            .withStyle(NIGHT_SHIFT)
            .withRadius(meters(1))
            .withDrawStrokeWidth(meters(0.2));
}
