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

package com.telenav.mesakit.map.ui.debug.theme;

import com.telenav.kivakit.ui.desktop.graphics.style.Style;

import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.ARUBA;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.MANHATTAN;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.MOJITO;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitStyles.SEATTLE;

/**
 * @author jonathanl (shibo)
 */
public class DebugViewerStyles
{
    public static final Style ACTIVE_BOX = ARUBA.withAlpha(128);

    public static final Style END = MANHATTAN;

    public static final Style INACTIVE_BOX = SEATTLE.transparent();

    public static final Style START = MOJITO;
}
