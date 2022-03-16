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

package com.telenav.mesakit.graph.matching.conflation;

import com.telenav.kivakit.core.value.level.Percent;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.measurements.geographic.Distance;

public class Conflation
{
    private final Edge base;

    private final Edge enhancing;

    private final Percent closeness;

    private final Distance overlap;

    Conflation(Edge enhancing, Edge base, Percent closeness, Distance overlap)
    {
        this.enhancing = enhancing;
        this.base = base;
        this.closeness = closeness;
        this.overlap = overlap;
    }

    public Edge base()
    {
        return base;
    }

    public Percent closeness()
    {
        return closeness;
    }

    public Edge enhancing()
    {
        return enhancing;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Conflation)
        {
            var that = (Conflation) object;
            return base.equals(that.base);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return base.hashCode();
    }

    public boolean isCloserThan(Conflation that)
    {
        return that == null || closeness().isGreaterThan(that.closeness());
    }

    public boolean isCloserThan(Percent closeness)
    {
        return closeness().isGreaterThan(closeness);
    }

    public Distance overlap()
    {
        return overlap;
    }

    @Override
    public String toString()
    {
        return "[Conflation enhancing = " + edge(enhancing()) + ", base = " + edge(base()) + ", closeness = "
                + closeness + "]";
    }

    private String edge(Edge edge)
    {
        return edge.identifier() + " (" + edge.displayRoadName() + ")";
    }
}
