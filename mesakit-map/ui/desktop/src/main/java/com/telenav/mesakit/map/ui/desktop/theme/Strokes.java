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

import static com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateDistance.units;
import static com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSystem.drawingSurface;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;

public class Strokes
{
    public static final MapStroke ROUNDED = MapStroke.stroke()
            .withCap(CAP_ROUND)
            .withJoin(JOIN_ROUND)
            .withWidth(units(drawingSurface(), 1));

    public static final MapStroke DASHED = ROUNDED.withDash(new float[] { 8 });

    public static final MapStroke DOTTED = ROUNDED.withDash(new float[] { 1 });
}
