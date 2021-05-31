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

package com.telenav.kivakit.navigation.routing.debuggers;

import com.telenav.kivakit.graph.Route;
import com.telenav.kivakit.graph.Vertex;
import com.telenav.kivakit.graph.collections.EdgeSet;
import com.telenav.kivakit.graph.ui.viewer.ViewableEdge;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.kernel.scalars.levels.Percentage;
import com.telenav.kivakit.kernel.time.Duration;
import com.telenav.kivakit.map.ui.swing.debug.InteractiveView;
import com.telenav.kivakit.map.ui.swing.debug.ViewableIdentifier;
import com.telenav.kivakit.map.ui.swing.debug.viewables.ViewableLocation;
import com.telenav.kivakit.map.ui.swing.debug.viewer.swing.SwingViewer;
import com.telenav.kivakit.navigation.routing.RoutingDebugger;
import com.telenav.kivakit.navigation.routing.RoutingRequest;
import com.telenav.kivakit.navigation.routing.RoutingResponse;
import com.telenav.kivakit.navigation.routing.cost.Cost;

import java.awt.Color;

public class SwingRoutingDebugger implements RoutingDebugger
{
    private static final Color WHITE = new Color(0xf0f0f0);

    private static final Color START = Color.GREEN.darker();

    private static final Color END = Color.RED.darker();

    private static final Color CURRENT = Color.RED;

    private static final Color EXPLORED = Color.ORANGE.darker();

    private static final Color FINAL = Color.GREEN.brighter();

    private final String title;

    private InteractiveView view;

    private final ViewableIdentifier current = new ViewableIdentifier("current");

    private final ViewableIdentifier finalRoute = new ViewableIdentifier("finalRoute");

    private final ViewableIdentifier startLabel = new ViewableIdentifier("start");

    private final ViewableIdentifier endLabel = new ViewableIdentifier("end");

    private final EdgeSet explored = new EdgeSet(Estimate._1024);

    private ConsoleRoutingDebugger console;

    public SwingRoutingDebugger(final String title)
    {
        this.title = title;
    }

    @Override
    public void onEnd(final RoutingRequest request, final RoutingResponse response)
    {
        if (console != null)
        {
            console.onEnd(request, response);
        }
        if (response.route() != null)
        {
            view.update(finalRoute, response.route().asViewable(Color.LIGHT_GRAY, FINAL, null));
            view.pullToFront(finalRoute);
        }
        view.pullToFront(startLabel);
        view.pullToFront(endLabel);
        view.frameComplete();
    }

    @Override
    public void onRelaxed(final Route route, final Cost cost)
    {
        for (final var edge : route)
        {
            if (!explored.contains(edge))
            {
                explored.add(edge);
                view.add(new ViewableEdge(edge, WHITE, EXPLORED, null));
            }
        }
        view.update(current, route.asViewable(WHITE, CURRENT, null));
        view.pullToFront(current);
        view.pullToFront(startLabel);
        view.pullToFront(endLabel);
        view.frameComplete();
        if (console != null)
        {
            console.onRelaxed(route, cost);
        }
        Duration.seconds(0.001).sleep();
    }

    @Override
    public void onSettled(final Vertex vertex, final Cost cost)
    {
        if (console != null)
        {
            console.onSettled(vertex, cost);
        }
    }

    @Override
    public void onStart(final RoutingRequest request)
    {
        if (console != null)
        {
            console.onStart(request);
        }
        if (view == null)
        {
            view = (InteractiveView) new SwingViewer().view(title);
            view.update(startLabel, new ViewableLocation(request.start().location(), START, WHITE, "start"));
            view.update(endLabel, new ViewableLocation(request.end().location(), END, WHITE, "end"));
            view.zoomToContents(Percentage.of(5));
            view.frameComplete();
        }
    }

    public SwingRoutingDebugger withConsoleOutput()
    {
        console = new ConsoleRoutingDebugger();
        return this;
    }
}
