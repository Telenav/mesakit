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
import com.telenav.mesakit.map.ui.swing.map.graphics.drawables.Line;

public class Lines
{
    public static final Line BASE = new Line()
            .withWidth(Width.meters(3))
            .withOutlineWidth(Width.meters(0.5));

    public static final Line IRON_GRAY = BASE
            .withStyle(Styles.IRON_GRAY)
            .withOutlineStyle(Styles.IRON_GRAY);

    public static final Line GRAYED_LIME = BASE
            .withStyle(Styles.GRAYED_LIME)
            .withOutlineStyle(Styles.GRAYED_LIME);

    public static final Line TANGERINE = BASE
            .withStyle(Styles.TANGERINE)
            .withOutlineStyle(Styles.TANGERINE);

    public static final Line TRANSLUCENT_YELLOW = BASE
            .withStyle(Styles.TRANSLUCENT_YELLOW)
            .withOutlineStyle(Styles.TRANSLUCENT_YELLOW);

    public static final Line TRANSLUCENT_LIME = BASE
            .withStyle(Styles.TRANSLUCENT_LIME)
            .withOutlineStyle(Styles.TRANSLUCENT_LIME);

    public static final Line TRANSLUCENT_TANGERINE = BASE
            .withStyle(Styles.TRANSLUCENT_TANGERINE)
            .withOutlineStyle(Styles.TRANSLUCENT_TANGERINE);
}
