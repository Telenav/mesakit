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

package com.telenav.mesakit.map.ui.debug.theme;

import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;

import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerStyles.END;
import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerStyles.START;

/**
 * @author jonathanl (shibo)
 */
public class DebugViewerIcons
{
    public static final MapDot END_ICON = MapDot.dot()
            .withStyle(END)
            .withRadius(Length.pixels(10))
            .withLabel("end");

    public static final MapDot START_ICON = MapDot.dot()
            .withStyle(START)
            .withRadius(Length.pixels(10))
            .withLabel("start");
}
