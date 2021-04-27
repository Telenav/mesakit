package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import static com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine.line;

/**
 * @author jonathanl (shibo)
 */
public class Lines
{
    public static final MapLine ACTIVE = line()
            .withStyle(MapStyles.ACTIVE);

    public static final MapLine GRAYED = line()
            .withStyle(MapStyles.GRAYED);

    public static final MapLine HIGHLIGHTED = line()
            .withStyle(MapStyles.HIGHLIGHTED);
}
