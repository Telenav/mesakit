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

package com.telenav.mesakit.map.ui.debug.viewer;

import com.telenav.kivakit.core.kernel.language.time.Duration;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

public interface InteractiveView extends View
{
    /**
     * Called to indicate the current frame has been updated. This allows the user to freeze and step through frames
     * with key strokes.
     */
    void frameComplete();

    /**
     * @param delay The delay between frames
     */
    void frameSpeed(Duration delay);

    /**
     * Moves the given viewable to the top of the view
     */
    void pullToFront(DrawableIdentifier identifier);

    /**
     * Moves the given viewable to the back of the view
     */
    void pushToBack(DrawableIdentifier identifier);

    /**
     * @param bounds The rectangle to view
     */
    void zoomTo(Rectangle bounds);

    /**
     * Zooms the view to include all contents
     */
    void zoomToContents(Percent margin);
}
