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

import com.telenav.mesakit.map.ui.desktop.graphics.canvas.Stroke;

import java.awt.BasicStroke;

public class Strokes
{
    public static final Stroke DASHED =
            new Stroke(BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 8 }, 0);

    public static final Stroke DOTTED =
            new Stroke(BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, new float[] { 1 }, 0);

    public static final Stroke ROUNDED =
            new Stroke(BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
}
