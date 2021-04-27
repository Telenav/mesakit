/*
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * //
 * // © 2011-2021 Telenav, Inc.
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * // http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 * //
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 *
 */

package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ARROWHEAD;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ERROR;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.RESTRICTION_STYLE;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ROUTE_STYLE;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.VIA_NODE_STYLE;

/**
 * @author jonathanl (shibo)
 */
public class Relations
{
    public static final MapLine RESTRICTION = MapLine.line()
            .withStyle(RESTRICTION_STYLE)
            .withToArrowHead(MapDot.dot(ARROWHEAD));

    public static final MapLine ROUTE = MapLine.line()
            .withStyle(ROUTE_STYLE)
            .withFillStrokeWidth(meters(5))
            .withDrawStrokeWidth(meters(0.5))
            .withToArrowHead(MapDot.dot(ARROWHEAD));

    public static final MapDot ROUTE_NONE = MapDot.dot(ERROR);

    public static final MapLine SELECTED_ROUTE = ROUTE
            .withColors(MapStyles.SELECTED_ROUTE);

    public static final MapDot VIA_NODE = MapDot.dot()
            .withStyle(VIA_NODE_STYLE);

    public static final MapDot VIA_NODE_BAD = MapDot.dot()
            .withStyle(ERROR);

    public static final MapDot VIA_NODE_SELECTED = MapDot.dot()
            .withStyle(MapStyles.SELECTED_ROUTE);
}
