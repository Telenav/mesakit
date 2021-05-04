package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Fonts;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.kivakit.ui.desktop.theme.KivaKitColors;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine;
import com.telenav.mesakit.map.ui.desktop.graphics.style.MapStroke;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import java.awt.Font;

import static com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color.TRANSPARENT;
import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;
import static com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine.line;

/**
 * @author jonathanl (shibo)
 */
public class Edges
{
    public static final Style CALLOUT = Style.create()
            .withTextFont(Fonts.fixedWidth(Font.BOLD, 12))
            .withFillColor(KivaKitColors.IRON.withAlpha(192))
            .withDrawColor(KivaKitColors.LIME)
            .withTextColor(KivaKitColors.LIME);

    public static final MapLine HIGHLIGHTED = line()
            .withStyle(MapStyles.HIGHLIGHTED
                    .withFillStroke(MapStroke.stroke(meters(10)))
                    .withDrawStroke(MapStroke.stroke(meters(1))));

    public static final MapLine NORMAL = line()
            .withStyle(MapStyles.NORMAL
                    .withFillStroke(MapStroke.stroke(meters(3)))
                    .withDrawStroke(MapStroke.stroke(meters(0.5))));

    public static final MapLine INACTIVE = NORMAL
            .withStyle(MapStyles.INACTIVE);

    public static final MapLine SELECTED = NORMAL
            .withStyle(MapStyles.SELECTED)
            .withToArrowHead(MapDot.dot()
                    .withRadius(meters(2))
                    .withStyle(MapStyles.ARROWHEAD
                            .withFillColor(TRANSPARENT)
                            .withDrawStroke(MapStroke.stroke(meters(0.1)))));
}
