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

package com.telenav.mesakit.navigation.routing.debuggers;

import com.telenav.kivakit.language.time.Duration;
import com.telenav.kivakit.language.count.Estimate;
import com.telenav.kivakit.language.level.Percent;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.ui.viewer.DrawableEdge;
import com.telenav.mesakit.graph.ui.viewer.DrawableLocation;
import com.telenav.mesakit.graph.ui.viewer.DrawableRoute;
import com.telenav.mesakit.map.ui.debug.DebugViewer;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;
import com.telenav.mesakit.navigation.routing.RoutingDebugger;
import com.telenav.mesakit.navigation.routing.RoutingRequest;
import com.telenav.mesakit.navigation.routing.RoutingResponse;
import com.telenav.mesakit.navigation.routing.cost.Cost;

public class SwingRoutingDebugger implements RoutingDebugger
{
    private final String title;

    private InteractiveView view;

    private final SwingRoutingDebuggerTheme theme = new SwingRoutingDebuggerTheme();

    private final DrawableIdentifier current = new DrawableIdentifier("current");

    private final DrawableIdentifier finalRoute = new DrawableIdentifier("finalRoute");

    private final DrawableIdentifier startLabel = new DrawableIdentifier("start");

    private final DrawableIdentifier endLabel = new DrawableIdentifier("end");

    private final EdgeSet explored = new EdgeSet(Estimate._1024);

    private ConsoleRoutingDebugger console;

    public SwingRoutingDebugger(String title)
    {
        this.title = title;
    }

    @Override
    public void onEnd(RoutingRequest request, RoutingResponse response)
    {
        if (console != null)
        {
            console.onEnd(request, response);
        }
        if (response.route() != null)
        {
            view.update(finalRoute, new DrawableRoute(response.route(), theme.styleFinal(), "final route"));
            view.pullToFront(finalRoute);
        }
        view.pullToFront(startLabel);
        view.pullToFront(endLabel);
        view.frameComplete();
    }

    @Override
    public void onRelaxed(Route route, Cost cost)
    {
        for (var edge : route)
        {
            if (!explored.contains(edge))
            {
                explored.add(edge);
                view.add(new DrawableEdge(edge, theme.styleExplored()));
            }
        }
        view.update(current, new DrawableRoute(route, theme.styleCurrent()));
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
    public void onSettled(Vertex vertex, Cost cost)
    {
        if (console != null)
        {
            console.onSettled(vertex, cost);
        }
    }

    @Override
    public void onStart(RoutingRequest request)
    {
        if (console != null)
        {
            console.onStart(request);
        }
        if (view == null)
        {
            view = (InteractiveView) new DebugViewer().view(title);
            view.update(startLabel, new DrawableLocation(request.start().location(), theme.styleStart(), "start"));
            view.update(endLabel, new DrawableLocation(request.end().location(), theme.styleEnd(), "end"));
            view.zoomToContents(Percent.of(5));
            view.frameComplete();
        }
    }

    public SwingRoutingDebugger withConsoleOutput()
    {
        console = new ConsoleRoutingDebugger();
        return this;
    }
}
