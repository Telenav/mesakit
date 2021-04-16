package com.telenav.mesakit.map.ui.swing.map.theme;

import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.swing.map.graphics.canvas.Style;
import com.telenav.mesakit.map.ui.swing.map.graphics.canvas.Width;
import com.telenav.mesakit.map.ui.swing.map.graphics.drawables.Dot;
import com.telenav.mesakit.map.ui.swing.map.graphics.drawables.Line;
import com.telenav.kivakit.ui.swing.graphics.color.Color;
import com.telenav.kivakit.ui.swing.graphics.color.KivaKitColors;
import com.telenav.kivakit.ui.swing.graphics.font.Fonts;

import java.awt.Font;

/**
 * @author jonathanl (shibo)
 */
public class MapStyles
{
    public static class Base
    {
        public static final Dot BASE_DOT = new Dot()
                .withWidth(Width.of(Distance.meters(5)))
                .withOutlineWidth(Width.of(Distance.meters(0.5)));

        public static final Line BASE_LINE = new Line()
                .withWidth(Width.of(Distance.meters(5)))
                .withOutlineWidth(Width.of(Distance.meters(0.5)));
    }

    public static class Debug
    {
        public static final Dot LOCATION = new Dot()
                .withWidth(Width.pixels(8f))
                .withStyle(Styles.TANGERINE.withFillColor(KivaKitColors.TRANSLUCENT_TANGERINE))
                .withOutlineWidth(Width.pixels(2f))
                .withOutlineStyle(Styles.TANGERINE.withDrawColor(KivaKitColors.IRON));

        public static final Style EDGE_CALLOUT = Styles.BASE
                .withFont(Fonts.fixedWidth(Font.BOLD, 12))
                .withFillColor(KivaKitColors.IRON.withAlpha(192))
                .withDrawColor(KivaKitColors.LIME)
                .withTextColor(KivaKitColors.LIME);

        public static final Style VERTEX_CALLOUT = Styles.BASE
                .withFont(Fonts.fixedWidth(Font.BOLD, 12))
                .withFillColor(KivaKitColors.IRON.withAlpha(192))
                .withDrawColor(KivaKitColors.LIME)
                .withTextColor(KivaKitColors.LIME);
    }

    public static class Edge
    {
        public static final Line HIGHLIGHTED = Lines.TRANSLUCENT_LIME
                .withOutlineWidth(Width.meters(0))
                .withWidth(Width.meters(10));

        public static final Line INACTIVE = Lines.IRON_GRAY
                .withOutlineWidth(Width.meters(0.5))
                .withWidth(Width.meters(3));

        public static final Line NORMAL = Lines.TANGERINE
                .withOutlineWidth(Width.meters(0.5))
                .withWidth(Width.meters(3));

        public static final Line SELECTED = Lines.TANGERINE
                .withOutlineWidth(Width.meters(0.5))
                .withWidth(Width.meters(3))
                .withFromArrow(Arrows.TRANSPARENT)
                .withToArrow(Arrows.AQUA);
    }

    public static class Polyline
    {
        public static final Line ZOOMED_IN = new Line()
                .withWidth(Width.meters(8.0f))
                .withStyle(Styles.TRANSLUCENT_LIME)
                .withOutlineWidth(Width.meters(1.0f))
                .withOutlineStyle(Styles.TRANSLUCENT_LIME);

        public static final Line ZOOMED_OUT = new Line()
                .withWidth(Width.pixels(3.0f))
                .withStyle(Styles.TRANSLUCENT_LIME)
                .withOutlineWidth(Width.pixels(1.0f))
                .withOutlineStyle(Styles.TRANSLUCENT_LIME);
    }

    public static class Place
    {
        public static final Style PLACE = Styles.BASE
                .withFillColor(KivaKitColors.TRANSLUCENT_CLOVER)
                .withDrawColor(KivaKitColors.AQUA.lightened());
    }

    public static class Relation
    {
        public static final Style RESTRICTION_STYLE = Styles.BASE
                .withFillColor(KivaKitColors.TRANSLUCENT_MARASCHINO)
                .withDrawColor(KivaKitColors.CHERRY);

        public static final Dot ROUTE_NONE = Dots.MARASCHINO;

        public static final Style ROUTE_STYLE = Styles.BASE
                .withFillColor(KivaKitColors.TRANSLUCENT_OCEAN)
                .withDrawColor(KivaKitColors.TRANSLUCENT_OCEAN);

        public static final Line ROUTE = new Line()
                .withWidth(Width.of(Distance.meters(5)))
                .withStyle(ROUTE_STYLE)
                .withOutlineWidth(Width.of(Distance.meters(0.5)))
                .withOutlineStyle(ROUTE_STYLE)
                .withFromArrow(Arrows.TRANSPARENT)
                .withToArrow(Arrows.AQUA);

        public static final Line RESTRICTION = Base.BASE_LINE
                .withStyle(RESTRICTION_STYLE)
                .withOutlineStyle(ROUTE_STYLE)
                .withFromArrow(Arrows.TRANSPARENT)
                .withToArrow(Arrows.AQUA);

        public static final Dot VIA_NODE_BAD = Dots.IRON_GRAY;

        public static final Style VIA_NODE_STYLE = Styles.BASE
                .withFillColor(KivaKitColors.TRANSLUCENT_MARASCHINO)
                .withDrawColor(KivaKitColors.CHERRY);

        public static final Dot VIA_NODE_SELECTED = Base.BASE_DOT
                .withStyle(Styles.TRANSLUCENT_YELLOW)
                .withOutlineStyle(VIA_NODE_STYLE);

        public static final Dot VIA_NODE = Base.BASE_DOT
                .withStyle(VIA_NODE_STYLE)
                .withOutlineStyle(VIA_NODE_STYLE);

        public static final Style SELECTED = Styles.BASE
                .withFillColor(KivaKitColors.YELLOW)
                .withDrawColor(KivaKitColors.CHERRY)
                .withTextColor(KivaKitColors.FOSSIL);

        public static final Line SELECTED_LINE = new Line()
                .withWidth(Width.of(Distance.meters(5)))
                .withStyle(Styles.TRANSLUCENT_YELLOW)
                .withOutlineWidth(Width.of(Distance.meters(1.0)))
                .withOutlineStyle(SELECTED)
                .withFromArrow(Arrows.TRANSPARENT)
                .withToArrow(Arrows.AQUA);
    }

    public static class Road
    {
        public static final Style BASE_STREET_LABEL = Styles.BASE
                .withFontSize(10)
                .withDrawColor(KivaKitColors.WHITE)
                .withTextColor(KivaKitColors.WHITE_SMOKE);

        public static final Color FIRST_CLASS = Color.rgb(205, 137, 139);

        public static final Color FIRST_CLASS_ZOOMED_OUT = KivaKitColors.AQUA;

        public static final Color SECOND_CLASS = Color.rgb(241, 204, 150);

        public static final Color THIRD_CLASS = Color.rgb(246, 248, 167);

        public static final Color FOURTH_CLASS = Color.rgb(254, 254, 254);

        public static final Color FREEWAY = Color.rgb(119, 146, 190);

        public static final Color FREEWAY_ZOOMED_OUT = KivaKitColors.AQUA;

        public static final Color HIGHWAY = Color.rgb(132, 204, 139);

        public static final Color HIGHWAY_ZOOMED_OUT = KivaKitColors.AQUA;

        public static final Style HIGHWAY_LABEL = BASE_STREET_LABEL
                .withFontSize(14)
                .withFillColor(KivaKitColors.HIGHWAY_SIGN_GREEN.withAlpha(192));

        public static final Style STREET_LABEL = BASE_STREET_LABEL
                .withFontSize(12)
                .withFillColor(KivaKitColors.HIGHWAY_SIGN_GREEN.withAlpha(192));

        public static final Style MINOR_STREET_LABEL = BASE_STREET_LABEL
                .withFontSize(10)
                .withFillColor(KivaKitColors.STREET_SIGN_BLUE.withAlpha(192));

        public static final Dot ROAD_NAME_CALLOUT_LOCATION = new Dot()
                .withWidth(Width.pixels(9f))
                .withStyle(Styles.OCEAN_AND_TANGERINE)
                .withOutlineWidth(Width.pixels(2f))
                .withOutlineStyle(Styles.OCEAN_AND_TANGERINE);
    }

    public static class ShapePoint
    {
        public static final Dot NORMAL = new Dot()
                .withWidth(Width.of(Distance.meters(1)))
                .withStyle(Styles.IRON_GRAY)
                .withOutlineWidth(Width.of(Distance.meters(0.2)))
                .withOutlineStyle(Styles.IRON_GRAY);

        public static final Dot SELECTED = new Dot()
                .withWidth(Width.of(Distance.meters(1)))
                .withStyle(Styles.TRANSLUCENT_TANGERINE)
                .withOutlineWidth(Width.of(Distance.meters(0.2)))
                .withOutlineStyle(Styles.TRANSLUCENT_TANGERINE);
    }

    public static class Vertex
    {
        public static final Dot NORMAL = Base.BASE_DOT
                .withStyle(Styles.TRANSLUCENT_OCEAN)
                .withOutlineStyle(Styles.TRANSLUCENT_OCEAN);

        public static final Dot SELECTED = Dots.TRANSLUCENT_TANGERINE;
    }
}

