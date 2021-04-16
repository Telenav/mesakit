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

package com.telenav.mesakit.map.ui.swing.debug;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;

import java.awt.Color;

/**
 * A {@link DrawingSurface} is a place where locations and lines can be drawn and labeled.
 *
 * @author jonathanl (shibo)
 */
public interface DrawingSurface
{
    /**
     * Draws a marker at the given location in the given color with the given label
     */
    void draw(Location location, Color background, Color foreground, String label);

    /**
     * Draws the given polyline in the given color with the given label
     */
    void draw(Polyline line, Color background, Color foreground, String label);
}
