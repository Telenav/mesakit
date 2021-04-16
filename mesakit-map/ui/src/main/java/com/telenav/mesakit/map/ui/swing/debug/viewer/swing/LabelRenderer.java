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

package com.telenav.mesakit.map.ui.swing.debug.viewer.swing;

import com.telenav.kivakit.ui.swing.graphics.font.Fonts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class LabelRenderer
{
    public static final Color TEXT_BACKGROUND = new Color(0x4e4e4e);

    public static final Color TEXT = new Color(0xf0f0f0);

    public static final Color TEXT_BACKGROUND_OUTLINE = TEXT;

    public enum Position
    {
        BOTTOM_RIGHT,
        TOP_LEFT
    }

    private final String label;

    public LabelRenderer(final String label)
    {
        this.label = label;
    }

    public void draw(final Graphics2D graphics, final Point2D at, final Position position, final Rectangle within)
    {
        graphics.setFont(Fonts.component(14));
        final var metrics = graphics.getFontMetrics();
        final var labelSize = metrics.getStringBounds(label, graphics);
        final var x = position == Position.BOTTOM_RIGHT ? (int) (at.getX() - labelSize.getWidth() - 12)
                : (int) at.getX();
        final var y = position == Position.BOTTOM_RIGHT ? (int) (at.getY() - 10) : (int) at.getY();
        final var backgroundX = x - 5;
        final var backgroundY = (int) (y - labelSize.getHeight() + metrics.getDescent() - 3);
        final var backgroundWidth = (int) labelSize.getWidth() + 10;
        final var backgroundHeight = (int) labelSize.getHeight() + metrics.getDescent();
        final var labelBounds = new Rectangle2D.Double(backgroundX - 5, backgroundY, backgroundWidth + 10,
                backgroundHeight);
        if (within.contains(labelBounds))
        {
            graphics.setStroke(new BasicStroke(1));
            graphics.setColor(TEXT_BACKGROUND);
            graphics.fillRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight);
            graphics.setColor(TEXT_BACKGROUND_OUTLINE);
            graphics.drawRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight);
            graphics.setColor(TEXT);
            graphics.drawString(label, x, y);
        }
    }
}
