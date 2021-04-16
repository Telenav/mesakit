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

package com.telenav.mesakit.map.ui.swing.debug.viewer.swing;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.swing.debug.InteractiveView;
import com.telenav.mesakit.map.ui.swing.debug.Viewable;
import com.telenav.mesakit.map.ui.swing.debug.ViewableIdentifier;
import com.telenav.mesakit.map.ui.swing.debug.viewables.ViewableLocation;
import com.telenav.mesakit.map.ui.swing.debug.viewer.empty.NullInteractiveView;
import com.telenav.kivakit.core.kernel.language.time.Duration;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;

import java.awt.Color;

/**
 * A {@link SwingViewer} with convenience methods to update typical {@link Viewable}s.
 *
 * @author jonathanl (shibo)
 */
public class DebugViewer extends SwingViewer
{
    private static final ViewableIdentifier START = new ViewableIdentifier("start");

    private static final ViewableIdentifier END = new ViewableIdentifier("end");

    private Color startColor = new Color(0xf0f0f0);

    private Color endColor = new Color(0xf0f0f0);

    private final InteractiveView view;

    public DebugViewer(final boolean debug)
    {
        view = debug ? (InteractiveView) new SwingViewer().view("debug") : new NullInteractiveView();
        frameSpeed(Duration.seconds(0.5));
    }

    public void clear()
    {
        view.clear();
    }

    public void end(final Location end)
    {
        view.update(END, new ViewableLocation(end, Color.RED.darker(), endColor, "end"));
    }

    public void endColor(final Color endColor)
    {
        this.endColor = endColor;
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
        view.update(START, new ViewableLocation(start, Color.GREEN.darker(), startColor, "start"));
    }

    public void startColor(final Color startColor)
    {
        this.startColor = startColor;
    }

    public InteractiveView view()
    {
        return view;
    }
}
