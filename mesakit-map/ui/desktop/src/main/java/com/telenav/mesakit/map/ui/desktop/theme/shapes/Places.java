package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;
import com.telenav.mesakit.map.ui.desktop.theme.MapStroke;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.GOLF;
import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;

/**
 * @author jonathanl (shibo)
 */
public class Places
{
    public static final MapDot NORMAL = MapDot.dot()
            .withStyle(GOLF.withFillStroke(MapStroke.stroke(meters(1))))
            .withRadius(meters(10));

    public static final MapDot SELECTED = NORMAL
            .withColors(MapStyles.SELECTED);
}
