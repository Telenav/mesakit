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

import com.telenav.mesakit.graph.Edge;

/**
 * @author jonathanl (shibo)
 */
public class ViewableEdge extends ViewablePolyline
{
    public ViewableEdge(final Edge edge, final Color background, final Color foreground, final String label)
    {
        super(edge.roadShape(), background, foreground, label);
    }
}
