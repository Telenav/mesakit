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

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapBox;

import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ACTIVE_BOX;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ACTIVE_LABEL;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.INACTIVE_BOX;

/**
 * @author jonathanl (shibo)
 */
public class Bounds
{
    public static final MapBox ACTIVE_BOUNDING_BOX = MapBox.box()
            .withLabelStyle(ACTIVE_LABEL)
            .withStyle(ACTIVE_BOX);

    public static final MapBox INACTIVE_BOUNDING_BOX = MapBox.box()
            .withLabelStyle(ACTIVE_LABEL)
            .withStyle(INACTIVE_BOX);
}
