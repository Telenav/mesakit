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

import com.telenav.kivakit.ui.desktop.graphics.style.Fonts;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.HIGHWAY_SIGN_GREEN;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.STREET_SIGN_BLUE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.WHITE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.WHITE_SMOKE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.HEADACHE;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.NORMAL;

/**
 * @author jonathanl (shibo)
 */
public class Roads
{
    public static final Style BASE = NORMAL
            .withTextFont(Fonts.component(10))
            .withDrawColor(WHITE)
            .withTextColor(WHITE_SMOKE);

    public static final Style HIGHWAY_LABEL = BASE
            .withTextFont(Fonts.component(14))
            .withFillColor(HIGHWAY_SIGN_GREEN.translucent());

    public static final Style MINOR_STREET_LABEL = BASE
            .withTextFont(Fonts.component(10))
            .withFillColor(STREET_SIGN_BLUE.translucent());

    public static final MapDot ROAD_NAME_CALLOUT = MapDot.dot()
            .withRadius(Length.pixels(9))
            .withStyle(HEADACHE)
            .withDrawStrokeWidth(Length.pixels(2));

    public static final Style STREET_LABEL = BASE
            .withTextFont(Fonts.component(12))
            .withFillColor(HIGHWAY_SIGN_GREEN.translucent());
}
