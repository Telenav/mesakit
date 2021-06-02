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

package com.telenav.mesakit.map.ui.debug;

import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.values.level.Percent;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDrawable;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;
import com.telenav.mesakit.map.ui.desktop.viewer.View;
import com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewer;

import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerIcons.END_ICON;
import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerIcons.START_ICON;

/**
 * A {@link DesktopViewer} with convenience methods to update typical {@link MapDrawable}s.
 *
 * @author jonathanl (shibo)
 */
public class DebugViewer extends DesktopViewer
{
    private static final DrawableIdentifier END = new DrawableIdentifier("end");

    private static final DrawableIdentifier START = new DrawableIdentifier("start");

    private InteractiveView view;

    public void clear()
    {
        view.clear();
    }

    public void end(final Location end)
    {
        view.update(END, END_ICON);
    }

    public void frameComplete()
    {
        view.zoomToContents(Percent.of(5));
        view.frameComplete();
    }

    public void frameSpeed(final Duration delay)
    {
        view.frameSpeed(delay);
    }

    public void start(final Location start)
    {
        view.update(START, START_ICON);
    }

    @Override
    public View view(final String title)
    {
        view = (InteractiveView) super.view(title);
        frameSpeed(Duration.seconds(0.5));
        return view;
    }

    public InteractiveView view()
    {
        return view;
    }
}
