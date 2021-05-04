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

import com.telenav.kivakit.ui.desktop.graphics.style.Style;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.DARK_TANGERINE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.DULL_BLUE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.IRON;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.KIVAKIT_BLACK;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.OCEAN;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.TRANSLUCENT_OCEAN;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.ARUBA;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.GOLF;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.MANHATTAN;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.MOJITO;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.NIGHT_SHIFT;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.OCEAN_SURF;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.SEATTLE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.SUNNY;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.VALENCIA;

public class MapStyles
{
    public static final Style ACTIVE = NIGHT_SHIFT;

    public static final Style ACTIVE_BOX = GOLF;

    public static final Style ARROWHEAD = ARUBA;

    public static final Style BASE = MOJITO;

    public static final Style MAP_BACKGROUND = BASE.withFillColor(KIVAKIT_BLACK);

    public static final Style ERROR = MANHATTAN;

    public static final Style GRAYED = SEATTLE;

    public static final Style GRID_LINES = BASE
            .withDrawColor(DARK_TANGERINE);

    public static final Style GRID_LABEL = BASE
            .withFillColor(DARK_TANGERINE.translucent())
            .withDrawColor(IRON)
            .withTextColor(IRON);

    public static final Style HIGHLIGHTED = NIGHT_SHIFT;

    public static final Style CAPTION = BASE
            .withTextColor(DARK_TANGERINE)
            .withDrawColor(DARK_TANGERINE)
            .withFillColor(DULL_BLUE);

    public static final Style INACTIVE = SEATTLE;

    public static final Style INACTIVE_BOX = SEATTLE.transparent();

    public static final Style ACTIVE_LABEL = OCEAN_SURF;

    public static final Style INACTIVE_LABEL = SEATTLE;

    public static final Style NORMAL = BASE;

    public static final Style RESTRICTION_STYLE = ERROR;

    public static final Style ROUTE_STYLE = BASE.
            withFillColor(TRANSLUCENT_OCEAN)
            .withDrawColor(OCEAN);

    public static final Style SELECTED = VALENCIA;

    public static final Style SELECTED_ROUTE = SUNNY;

    public static final Style VIA_NODE_STYLE = MANHATTAN;
}
