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

package com.telenav.mesakit.graph.ui.debuggers.edge.sectioner;

import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.values.level.Percent;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Rainbow;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.EdgeSection;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.EdgeSectionerDebugger;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;
import com.telenav.mesakit.map.ui.desktop.viewer.empty.NullInteractiveView;

import java.awt.Color;

/**
 * @author jonathanl (shibo)
 */
public class VisualEdgeSectionDebugger implements EdgeSectionerDebugger
{
    private static final Rainbow DEBUG_COLORS = new Rainbow();

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
            debugView.zoomToContents(new Percent(10));
            debugView.frameComplete();
        }
    }
}
