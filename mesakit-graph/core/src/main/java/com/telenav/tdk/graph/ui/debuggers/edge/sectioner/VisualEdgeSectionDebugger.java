////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.tdk.graph.ui.debuggers.edge.sectioner;

import com.telenav.tdk.core.kernel.scalars.levels.Percentage;
import com.telenav.tdk.core.kernel.time.Duration;
import com.telenav.tdk.graph.specifications.osm.graph.loader.sectioner.EdgeSection;
import com.telenav.tdk.graph.specifications.osm.graph.loader.sectioner.EdgeSectionerDebugger;
import com.telenav.tdk.map.ui.swing.debug.InteractiveView;
import com.telenav.tdk.map.ui.swing.debug.ViewableIdentifier;
import com.telenav.tdk.map.ui.swing.debug.viewables.ViewablePolyline;
import com.telenav.tdk.map.ui.swing.debug.viewer.ColorSequence;
import com.telenav.tdk.map.ui.swing.debug.viewer.empty.NullInteractiveView;
import com.telenav.tdk.map.ui.swing.debug.viewer.swing.SwingViewer;

import java.awt.*;

/**
 * @author jonathanl (shibo)
 */
public class VisualEdgeSectionDebugger implements EdgeSectionerDebugger
{
    private static final ColorSequence DEBUG_COLORS = new ColorSequence();

    /** Debugger */
    private InteractiveView debugView = new NullInteractiveView();

    /** True if we're debugging */
    private boolean debugging;

    /** True if we're in the process of viewing the steps of sectioning a particular edge */
    private boolean viewing;

    @Override
    public void start()
    {
        if (!debugging)
        {
            debugging = true;
            final var viewer = new SwingViewer();
            debugView = (InteractiveView) viewer.view("debug");
            debugView.frameSpeed(Duration.ONE_SECOND);
        }
        debugView.clear();
        viewing = true;
        debugView.clear();
    }

    @Override
    public void stop()
    {
        viewing = false;
    }

    @Override
    public void update(final EdgeSection section)
    {
        if (viewing)
        {
            final var identifier = section.edge().identifier();
            debugView.update(new ViewableIdentifier(identifier), new ViewablePolyline(section.shape(),
                    Color.LIGHT_GRAY, DEBUG_COLORS.next(), "Edge " + identifier));
            debugView.zoomToContents(new Percentage(10));
            debugView.frameComplete();
        }
    }
}
