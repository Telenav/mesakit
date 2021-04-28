/*
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * //
 * // © 2011-2021 Telenav, Inc.
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * // http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 * //
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 *
 */

package com.telenav.mesakit.map.ui.debug.viewer.desktop.theme;

import com.telenav.kivakit.ui.desktop.graphics.style.Style;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.TRANSPARENT;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.GOLF;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.MANHATTAN;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.MOJITO;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.SEATTLE;

/**
 * @author jonathanl (shibo)
 */
public class DebugViewerStyles
{
    public static final Style ACTIVE_BOX = GOLF
            .withFillColor(TRANSPARENT);

    public static Style END = MANHATTAN;

    public static Style INACTIVE_BOX = SEATTLE.withFillColor(TRANSPARENT);

    public static Style START = MOJITO;
}
