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

package com.telenav.mesakit.graph.ui.viewer;

import com.telenav.kivakit.ui.desktop.graphics.drawing.Drawable;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.ui.viewer.theme.GraphDebugViewerTheme;
import com.telenav.mesakit.map.ui.debug.DebugViewer;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;

/**
 * A {@link DebugViewer} with convenience methods to update typical {@link Drawable}s.
 *
 * @author jonathanl (shibo)
 */
public class GraphDebugViewer extends DebugViewer
{
    private static final DrawableIdentifier CURRENT_EDGE = new DrawableIdentifier("currentEdge");

    private static final DrawableIdentifier CANDIDATE_EDGE = new DrawableIdentifier("candidateEdge");

    private static final DrawableIdentifier HIGHLIGHT_EDGE = new DrawableIdentifier("highlightEdge");

    private static final DrawableIdentifier CURRENT_ROUTE = new DrawableIdentifier("currentRoute");

    private static final DrawableIdentifier CANDIDATE_ROUTE = new DrawableIdentifier("candidateRoute");

    private static final DrawableIdentifier HIGHLIGHT_ROUTE = new DrawableIdentifier("highlightRoute");

    private final GraphDebugViewerTheme theme = new GraphDebugViewerTheme();

    public GraphDebugViewer()
    {
    }

    public void candidate(Edge edge)
    {
        candidate(edge, "candidate");
    }

    public void candidate(Edge edge, String label)
    {
        view().update(CANDIDATE_EDGE, new DrawableEdge(edge, theme.styleCandidate(), label));
    }

    public void candidate(Route route)
    {
        candidate(route, "candidate");
    }

    public void candidate(Route route, String label)
    {

        view().update(CANDIDATE_EDGE, new DrawableRoute(route, theme.styleCandidate(), label));
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

    public void current(Edge edge)
    {
        current(edge, "current");
    }

    public void current(Edge edge, String label)
    {
        view().update(CURRENT_EDGE, new DrawableEdge(edge, theme.styleCurrent(), label));
    }

    public void current(Route route)
    {
        current(route, "current");
    }

    public void current(Route route, String label)
    {
        view().update(CURRENT_ROUTE, new DrawableRoute(route, theme.styleCurrent(), label));
    }

    public void edge(Edge edge, String label)
    {
        view().add(new DrawableEdge(edge, theme.styleEdge(), label));
    }

    public void highlight(Edge edge, String label)
    {
        view().update(HIGHLIGHT_EDGE, new DrawableEdge(edge, theme.styleHighlight(), label));
    }

    public void highlight(Route route, String label)
    {
        view().update(HIGHLIGHT_ROUTE, new DrawableRoute(route, theme.styleRoute(), label));
    }

    public void route(Route route, String label)
    {
        view().add(new DrawableRoute(route, theme.styleRoute(), label));
    }
}
