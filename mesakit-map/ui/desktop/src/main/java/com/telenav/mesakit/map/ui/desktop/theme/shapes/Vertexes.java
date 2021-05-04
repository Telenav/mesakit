package com.telenav.mesakit.map.ui.desktop.theme.shapes;

import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Fonts;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDot;
import com.telenav.mesakit.map.ui.desktop.graphics.style.MapStroke;
import com.telenav.mesakit.map.ui.desktop.theme.MapStyles;

import java.awt.Font;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.IRON;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.ABSINTHE;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.LIFEBUOY;
import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;

/**
 * @author jonathanl (shibo)
 */
public class Vertexes
{
    public static final Style CALLOUT = ABSINTHE
            .withTextFont(Fonts.fixedWidth(Font.BOLD, 12))
            .withFillColor(IRON.withAlpha(192));

    public static final MapDot NORMAL = MapDot.dot()
            .withRadius(meters(5))
            .withStyle(LIFEBUOY
                    .withFillStroke(MapStroke.stroke(meters(0.5))));

    public static final MapDot DISABLED = NORMAL.withColors(MapStyles.GRAYED);

    public static final MapDot INVALID = NORMAL.withColors(MapStyles.ERROR);

    public static final MapDot SELECTED = NORMAL.withColors(MapStyles.SELECTED);
}
