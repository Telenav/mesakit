////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.ui.swing.debug;

import com.telenav.kivakit.core.kernel.interfaces.naming.Named;

/**
 * A view is a named repository of {@link Viewable} objects which can be updated.
 *
 * @author jonathanl (shibo)
 */
public interface View extends Named
{
    /**
     * Adds the given object to the view. Since it is not identified, it can never be removed or updated.
     */
    void add(Viewable object);

    /**
     * Clears the set of {@link Viewable} objects held by this view. Not all views necessarily support this operation.
     * If the view does not support the operation, calling this method will have no effect.
     */
    void clear();

    /**
     * Remove the identified {@link Viewable} object from the view
     */
    void remove(ViewableIdentifier identifier);

    /**
     * Updates the identified {@link Viewable} object in this view.
     */
    void update(ViewableIdentifier identifier, Viewable object);
}
