package com.telenav.mesakit.graph.ui.viewer.theme;

import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.kivakit.ui.desktop.theme.KivaKitColors;
import com.telenav.kivakit.ui.desktop.theme.darcula.KivaKitDarculaTheme;

public class GraphDebugViewerTheme extends KivaKitDarculaTheme
{
    public Style styleCandidate()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.HIGHWAY_SIGN_GREEN);
    }

    public Style styleCurrent()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.BLUE_RIDGE_MOUNTAINS);
    }

    public Style styleEdge()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.CLOVER);
    }

    public Style styleHighlight()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.MARASCHINO);
    }

    public Style styleRoute()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.CLOVER);
    }
}
