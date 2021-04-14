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

package com.telenav.aonia.map.ui.swing.map.theme;

import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Width;
import com.telenav.aonia.map.ui.swing.map.graphics.drawables.Arrow;
import com.telenav.aonia.map.ui.swing.map.graphics.drawables.Dot;
import com.telenav.kivakit.ui.swing.graphics.color.KivaKitColors;

public class Arrows
{
    private static final Dot BASE = new Dot()
            .withWidth(Width.meters(3))
            .withStyle(Styles.AQUA_OUTLINE.withDrawColor(KivaKitColors.AQUA))
            .withOutlineWidth(Width.meters(0.5))
            .withOutlineStyle(Styles.AQUA);

    public static final Arrow AQUA = new Arrow(BASE, Styles.AQUA);

    public static final Arrow TRANSPARENT = new Arrow(BASE, Styles.TRANSPARENT);
}
