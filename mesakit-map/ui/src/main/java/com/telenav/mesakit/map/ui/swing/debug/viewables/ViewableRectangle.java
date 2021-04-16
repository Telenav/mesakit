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

package com.telenav.mesakit.map.ui.swing.debug.viewables;

import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.swing.debug.DrawingSurface;
import com.telenav.mesakit.map.ui.swing.debug.Viewable;

import java.awt.Color;

/**
 * @author jonathanl (shibo)
 */
public class ViewableRectangle implements Viewable
{
    private final Rectangle rectangle;

    private final Color background;

    private final Color foreground;

    private final String label;

    public ViewableRectangle(final Rectangle rectangle,
                             final Color background,
                             final Color foreground,
                             final String label)
    {
        this.rectangle = rectangle;
        this.background = background;
        this.foreground = foreground;
        this.label = label;
    }

    @Override
    public Rectangle bounds()
    {
        return rectangle;
    }

    @Override
    public void draw(final DrawingSurface surface)
    {
        surface.draw(rectangle.asPolyline(), background, foreground, label);
    }
}
