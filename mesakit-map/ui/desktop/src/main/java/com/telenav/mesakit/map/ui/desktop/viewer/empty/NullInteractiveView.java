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

package com.telenav.mesakit.map.ui.desktop.viewer.empty;

import com.telenav.kivakit.core.time.Duration;

import com.telenav.kivakit.core.value.level.Percent;

import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDrawable;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;

import java.util.function.Function;

public class NullInteractiveView implements InteractiveView
{
    @Override
    public void add(MapDrawable drawable)
    {
    }

    @Override
    public void clear()
    {
    }

    @Override
    public void frameComplete()
    {
    }

    @Override
    public void frameSpeed(Duration delay)
    {
    }

    @Override
    public void map(Function<MapDrawable, MapDrawable> function)
    {
    }

    @Override
    public void pullToFront(DrawableIdentifier identifier)
    {
    }

    @Override
    public void pushToBack(DrawableIdentifier identifier)
    {
    }

    @Override
    public void remove(DrawableIdentifier identifier)
    {

    }

    @Override
    public void update(DrawableIdentifier identifier, MapDrawable object)
    {
    }

    @Override
    public void zoomTo(Rectangle bounds)
    {
    }

    @Override
    public void zoomToContents(Percent margin)
    {
    }
}
