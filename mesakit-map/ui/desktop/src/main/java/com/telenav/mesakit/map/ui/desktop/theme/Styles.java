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

package com.telenav.mesakit.map.ui.desktop.theme;

import com.telenav.kivakit.ui.swing.graphics.font.Fonts;
import com.telenav.kivakit.ui.swing.graphics.style.Color;
import com.telenav.kivakit.ui.swing.theme.KivaKitColors;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.Style;

public class Styles
{
    public static final Style BASE = new Style()
            .withFillColor(KivaKitColors.UNSPECIFIED)
            .withFillStroke(Strokes.ROUNDED)
            .withDrawColor(KivaKitColors.UNSPECIFIED)
            .withDrawStroke(Strokes.ROUNDED)
            .withTextColor(KivaKitColors.UNSPECIFIED)
            .withFont(Fonts.component(12));

    public static final Style AQUA = BASE
            .withFillColor(KivaKitColors.AQUA)
            .withDrawColor(KivaKitColors.AQUA)
            .withTextColor(KivaKitColors.AQUA);

    public static final Style AQUA_OUTLINE = BASE
            .withFillColor(Color.TRANSPARENT)
            .withDrawColor(KivaKitColors.AQUA)
            .withTextColor(KivaKitColors.AQUA);

    public static final Style DARK_GRAY = BASE
            .withFillColor(KivaKitColors.BLACK_OLIVE)
            .withDrawColor(KivaKitColors.BLACK_OLIVE)
            .withTextColor(KivaKitColors.BLACK_OLIVE);

    public static final Style GRAY = BASE
            .withFillColor(KivaKitColors.FOSSIL)
            .withDrawColor(KivaKitColors.FOSSIL)
            .withTextColor(KivaKitColors.FOSSIL);

    public static final Style GRAYED_LIME = BASE
            .withFillColor(KivaKitColors.LIME)
            .withDrawColor(KivaKitColors.FOSSIL)
            .withTextColor(KivaKitColors.FOSSIL);

    public static final Style IRON_GRAY = BASE
            .withFillColor(KivaKitColors.FOSSIL)
            .withDrawColor(KivaKitColors.IRON)
            .withTextColor(KivaKitColors.IRON);

    public static final Style LIME = BASE
            .withFillColor(KivaKitColors.LIME)
            .withDrawColor(KivaKitColors.LIME)
            .withTextColor(KivaKitColors.LIME);

    public static final Style MARASCHINO = BASE
            .withFillColor(KivaKitColors.CHERRY)
            .withDrawColor(Color.WHITE)
            .withTextColor(Color.WHITE);

    public static final Style OCEAN_AND_WHITE = BASE
            .withFillColor(KivaKitColors.OCEAN)
            .withDrawColor(Color.WHITE)
            .withTextColor(Color.WHITE);

    public static final Style OCEAN_AND_TANGERINE = BASE
            .withFillColor(KivaKitColors.TRANSLUCENT_OCEAN)
            .withDrawColor(KivaKitColors.TANGERINE)
            .withTextColor(KivaKitColors.TANGERINE);

    public static final Style TANGERINE = BASE
            .withFillColor(KivaKitColors.TANGERINE)
            .withDrawColor(KivaKitColors.FOSSIL)
            .withTextColor(KivaKitColors.AQUA);

    public static final Style TRANSLUCENT_CLOVER = BASE
            .withFillColor(KivaKitColors.TRANSLUCENT_CLOVER)
            .withDrawColor(KivaKitColors.CLOVER)
            .withTextColor(KivaKitColors.BLACK_OLIVE);

    public static final Style TRANSLUCENT_OCEAN = BASE
            .withFillColor(KivaKitColors.TRANSLUCENT_OCEAN)
            .withDrawColor(KivaKitColors.TANGERINE)
            .withTextColor(KivaKitColors.IRON);

    public static final Style TRANSLUCENT_TANGERINE = BASE
            .withFillColor(KivaKitColors.TRANSLUCENT_TANGERINE)
            .withDrawColor(KivaKitColors.OCEAN)
            .withTextColor(KivaKitColors.IRON);

    public static final Style TRANSLUCENT_YELLOW = BASE
            .withFillColor(KivaKitColors.TRANSLUCENT_YELLOW)
            .withDrawColor(KivaKitColors.YELLOW)
            .withTextColor(KivaKitColors.BLACK_OLIVE);

    public static final Style TRANSLUCENT_LIME = BASE
            .withFillColor(KivaKitColors.TRANSLUCENT_LIME)
            .withDrawColor(KivaKitColors.LIME)
            .withTextColor(KivaKitColors.BLACK_OLIVE);

    public static final Style TRANSPARENT = BASE
            .withFillColor(Color.TRANSPARENT)
            .withDrawColor(Color.TRANSPARENT)
            .withTextColor(Color.TRANSPARENT);
}
