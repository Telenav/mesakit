package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine;
import com.telenav.mesakit.map.ui.desktop.theme.MapStroke;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;

/**
 * @author jonathanl (shibo)
 */
public class Polylines
{
    public static final MapLine ZOOMED_IN = MapLine.line()
            .withStyle(MapStyles.NORMAL
                    .withFillStroke(MapStroke.stroke(meters(8.0)))
                    .withDrawStroke(MapStroke.stroke(meters(1.0))));

    public static final MapLine ZOOMED_OUT = MapLine.line()
            .withStyle(MapStyles.NORMAL
                    .withFillStroke(MapStroke.stroke(meters(3.0)))
                    .withDrawStroke(MapStroke.stroke(meters(1.0))));
}
