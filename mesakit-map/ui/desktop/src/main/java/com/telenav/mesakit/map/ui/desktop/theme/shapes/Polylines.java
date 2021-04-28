package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapPolyline;
import com.telenav.mesakit.map.ui.desktop.theme.MapStroke;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;

/**
 * @author jonathanl (shibo)
 */
public class Polylines
{
    public static final MapPolyline NORMAL = MapPolyline.polyline()
            .withStyle(MapStyles.NORMAL
                    .withFillStroke(MapStroke.stroke(meters(8.0)))
                    .withDrawStroke(MapStroke.stroke(meters(1.0))));

    public static final MapPolyline ZOOMED_IN = MapPolyline.polyline()
            .withStyle(MapStyles.NORMAL
                    .withFillStroke(MapStroke.stroke(meters(8.0)))
                    .withDrawStroke(MapStroke.stroke(meters(1.0))));

    public static final MapPolyline ZOOMED_OUT = MapPolyline.polyline()
            .withStyle(MapStyles.NORMAL
                    .withFillStroke(MapStroke.stroke(meters(3.0)))
                    .withDrawStroke(MapStroke.stroke(meters(1.0))));
}
