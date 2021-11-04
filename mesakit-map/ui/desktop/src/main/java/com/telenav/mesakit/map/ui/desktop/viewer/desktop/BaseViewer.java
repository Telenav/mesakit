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

package com.telenav.mesakit.map.ui.desktop.viewer.desktop;

import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.mesakit.map.ui.desktop.viewer.View;
import com.telenav.mesakit.map.ui.desktop.viewer.Viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseViewer extends BaseRepeater implements Viewer
{
    private final List<View> views = new ArrayList<>();

    @Override
    public View view(String title)
    {
        for (var view : views)
        {
            if (view.name().equals(title))
            {
                return view;
            }
        }
        var view = newView(title);
        views.add(view);
        return view;
    }

    @Override
    public Collection<View> views()
    {
        return views;
    }

    protected abstract View newView(String title);
}
