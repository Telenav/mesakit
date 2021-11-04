package com.telenav.mesakit.navigation.routing.debuggers;

import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.kivakit.ui.desktop.theme.KivaKitColors;
import com.telenav.kivakit.ui.desktop.theme.darcula.KivaKitDarculaTheme;

public class SwingRoutingDebuggerTheme extends KivaKitDarculaTheme
{
    public Style styleCurrent()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.MARASCHINO);
    }

    public Style styleEnd()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.MARASCHINO);
    }

    public Style styleExplored()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.TANGERINE);
    }

    public Style styleFinal()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.CLOVER);
    }

    public Style styleStart()
    {
        return styleLabel()
                .withFillColor(KivaKitColors.LIGHT_GRAY)
                .withTextColor(KivaKitColors.HIGHWAY_SIGN_GREEN);
    }
}
