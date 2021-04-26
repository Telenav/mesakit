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

import com.telenav.kivakit.ui.swing.graphics.style.Color;
import com.telenav.kivakit.ui.swing.theme.KivaKitColors;
import com.telenav.kivakit.ui.swing.theme.KivaKitTheme;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.Style;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.Width;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.Label;

public class Labels
{
    public static final Style STYLE_BORDERED = Styles.BASE
            .withDrawColor(KivaKitColors.AQUA)
            .withFillColor(Color.TRANSPARENT)
            .withTextColor(KivaKitColors.AQUA)
            .withFont(KivaKitTheme.get().fontNormal());

    public static final Style STYLE_GRAYED = Styles.BASE
            .withDrawColor(KivaKitColors.FOSSIL)
            .withFillColor(Color.TRANSPARENT)
            .withTextColor(KivaKitColors.FOSSIL)
            .withFont(KivaKitTheme.get().fontNormal());

    public static final Style STYLE_NORMAL = Styles.BASE
            .withDrawColor(KivaKitColors.AQUA)
            .withFillColor(KivaKitColors.TRANSLUCENT_CLOVER)
            .withTextColor(KivaKitColors.AQUA)
            .withFont(KivaKitTheme.get().fontNormal());

    public static final Label SELECTED = new Label(Width.pixels(1.5f), Styles.TRANSLUCENT_YELLOW);

    public static final Label NORMAL = new Label(Width.pixels(1.5f), STYLE_NORMAL);
}
