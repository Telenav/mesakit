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

package com.telenav.kivakit.graph.ui.viewer;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.Route;
import com.telenav.kivakit.map.ui.swing.debug.Viewable;
import com.telenav.kivakit.map.ui.swing.debug.ViewableIdentifier;
import com.telenav.kivakit.map.ui.swing.debug.viewer.swing.DebugViewer;
import com.telenav.kivakit.map.ui.swing.debug.viewer.swing.SwingViewer;

import java.awt.*;

/**
 * A {@link SwingViewer} with convenience methods to update typical {@link Viewable}s.
 *
 * @author jonathanl (shibo)
 */
public class GraphDebugViewer extends DebugViewer
{
    private static final ViewableIdentifier CURRENT_EDGE = new ViewableIdentifier("currentEdge");

    private static final ViewableIdentifier CANDIDATE_EDGE = new ViewableIdentifier("candidateEdge");

    private static final ViewableIdentifier HIGHLIGHT_EDGE = new ViewableIdentifier("highlightEdge");

    private static final ViewableIdentifier CURRENT_ROUTE = new ViewableIdentifier("currentRoute");

    private static final ViewableIdentifier CANDIDATE_ROUTE = new ViewableIdentifier("candidateRoute");

    private static final ViewableIdentifier HIGHLIGHT_ROUTE = new ViewableIdentifier("highlightRoute");

    private Color highlightColor = Color.RED;

    private Color currentColor = Color.BLUE;

    private Color candidateColor = Color.GREEN;

    private Color edgeColor = Color.GREEN;

    private Color routeColor = Color.GREEN;

    public GraphDebugViewer(final boolean debug)
    {
        super(debug);
    }

    public void candidate(final Edge edge)
    {
        candidate(edge, "candidate");
    }

    public void candidate(final Edge edge, final String label)
    {
        view().update(CANDIDATE_EDGE, new ViewableEdge(edge, Color.LIGHT_GRAY, candidateColor, label));
    }

    public void candidate(final Route route)
    {
        candidate(route, "candidate");
    }

    public void candidate(final Route route, final String label)
    {
        view().update(CANDIDATE_EDGE, route.asViewable(Color.LIGHT_GRAY, candidateColor, label));
    }

    public void candidateColor(final Color candidateColor)
    {
        this.candidateColor = candidateColor;
    }

    public void clearCandidateEdge()
    {
        view().remove(CANDIDATE_EDGE);
    }

    public void clearCandidateRoute()
    {
        view().remove(CANDIDATE_ROUTE);
    }

    public void clearCurrentEdge()
    {
        view().remove(CURRENT_EDGE);
    }

    public void clearCurrentRoute()
    {
        view().remove(CURRENT_ROUTE);
    }

    public void clearHighlightEdge()
    {
        view().remove(HIGHLIGHT_EDGE);
    }

    public void clearHighlightRoute()
    {
        view().remove(HIGHLIGHT_ROUTE);
    }

    public void current(final Edge edge)
    {
        current(edge, "current");
    }

    public void current(final Edge edge, final String label)
    {
        view().update(CURRENT_EDGE, new ViewableEdge(edge, Color.LIGHT_GRAY, currentColor, label));
    }

    public void current(final Route route)
    {
        current(route, "current");
    }

    public void current(final Route route, final String label)
    {
        view().update(CURRENT_ROUTE, route.asViewable(Color.LIGHT_GRAY, currentColor, label));
    }

    public void currentColor(final Color currentColor)
    {
        this.currentColor = currentColor;
    }

    public void edge(final Edge edge, final String label)
    {
        view().add(new ViewableEdge(edge, Color.LIGHT_GRAY, edgeColor, label));
    }

    public void edgeColor(final Color edgeColor)
    {
        this.edgeColor = edgeColor;
    }

    public void highlight(final Edge edge, final String label)
    {
        view().update(HIGHLIGHT_EDGE, new ViewableEdge(edge, Color.LIGHT_GRAY, highlightColor, label));
    }

    public void highlight(final Route edge, final String label)
    {
        view().update(HIGHLIGHT_ROUTE, edge.asViewable(Color.LIGHT_GRAY, highlightColor, label));
    }

    public void highlightColor(final Color highlightColor)
    {
        this.highlightColor = highlightColor;
    }

    public void route(final Route route, final String label)
    {
        view().add(route.asViewable(Color.LIGHT_GRAY, routeColor, label));
    }

    public void routeColor(final Color routeColor)
    {
        this.routeColor = routeColor;
    }
}
