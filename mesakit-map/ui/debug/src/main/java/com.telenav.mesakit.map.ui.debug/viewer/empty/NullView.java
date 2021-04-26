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

package com.telenav.mesakit.map.ui.desktop.debug.viewer.empty;

import com.telenav.mesakit.map.ui.desktop.debug.View;
import com.telenav.mesakit.map.ui.desktop.debug.Viewable;
import com.telenav.mesakit.map.ui.desktop.debug.ViewableIdentifier;

public class NullView implements View
{
    @Override
    public void add(final Viewable object)
    {
    }

    @Override
    public void clear()
    {
    }

    @Override
    public String name()
    {
        return "null";
    }

    @Override
    public void remove(final ViewableIdentifier identifier)
    {
    }

    @Override
    public void update(final ViewableIdentifier identifier, final Viewable object)
    {
    }
}
