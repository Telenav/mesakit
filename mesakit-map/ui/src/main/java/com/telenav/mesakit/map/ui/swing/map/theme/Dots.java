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

package com.telenav.mesakit.map.ui.swing.map.theme;

import com.telenav.mesakit.map.ui.swing.map.graphics.canvas.Width;
import com.telenav.mesakit.map.ui.swing.map.graphics.drawables.Dot;

public class Dots
{
    public static final Dot BASE = new Dot()
            .withWidth(Width.meters(5))
            .withOutlineWidth(Width.meters(0.5));

    public static final Dot IRON_GRAY = BASE
            .withStyle(Styles.IRON_GRAY)
            .withOutlineStyle(Styles.IRON_GRAY);

    public static final Dot MARASCHINO = BASE
            .withStyle(Styles.MARASCHINO)
            .withOutlineStyle(Styles.MARASCHINO);

    public static final Dot TANGERINE = BASE
            .withStyle(Styles.TANGERINE)
            .withOutlineStyle(Styles.TANGERINE);

    public static final Dot TRANSLUCENT_TANGERINE = BASE
            .withStyle(Styles.TRANSLUCENT_TANGERINE)
            .withOutlineStyle(Styles.TRANSLUCENT_TANGERINE);

    public static final Dot TRANSLUCENT_OCEAN = BASE
            .withStyle(Styles.TRANSLUCENT_OCEAN)
            .withOutlineStyle(Styles.TRANSLUCENT_TANGERINE);

    public static final Dot TRANSLUCENT_CLOVER = BASE
            .withStyle(Styles.TRANSLUCENT_CLOVER)
            .withOutlineStyle(Styles.TRANSLUCENT_CLOVER);

    public static final Dot TRANSLUCENT_YELLOW = BASE
            .withStyle(Styles.TRANSLUCENT_YELLOW)
            .withOutlineStyle(Styles.TRANSLUCENT_YELLOW);
}
