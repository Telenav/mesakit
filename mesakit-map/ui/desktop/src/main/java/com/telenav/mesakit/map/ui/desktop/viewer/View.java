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

package com.telenav.mesakit.map.ui.desktop.viewer;

import com.telenav.kivakit.core.kernel.interfaces.naming.Named;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDrawable;

import java.util.function.Function;

/**
 * A view is a named repository of {@link MapDrawable} objects which can be updated.
 *
 * @author jonathanl (shibo)
 */
public interface View extends Named
{
    /**
     * Adds the given object to the view. Since it is not identified, it can never be removed or updated.
     */
    void add(MapDrawable drawable);

    /**
     * Clears the set of {@link MapDrawable} objects held by this view. Not all views necessarily support this
     * operation. If the view does not support the operation, calling this method will have no effect.
     */
    void clear();

    void map(Function<MapDrawable, MapDrawable> consumer);

    /**
     * Moves the given viewable to the top of the view
     */
    void pullToFront(DrawableIdentifier identifier);

    /**
     * Moves the given viewable to the back of the view
     */
    void pushToBack(DrawableIdentifier identifier);

    /**
     * Remove the identified {@link MapDrawable} object from the view
     */
    void remove(DrawableIdentifier identifier);

    /**
     * Updates the identified {@link MapDrawable} object in this view.
     */
    void update(DrawableIdentifier identifier, MapDrawable object);
}
