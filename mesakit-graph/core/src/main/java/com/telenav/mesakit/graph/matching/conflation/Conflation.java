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

import com.telenav.kivakit.kernel.scalars.levels.Percent;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.measurements.geographic.Distance;

public class Conflation
{
    private final Edge base;

    private final Edge enhancing;

    private final Percent closeness;

    private final Distance overlap;

    Conflation(final Edge enhancing, final Edge base, final Percent closeness, final Distance overlap)
    {
        this.enhancing = enhancing;
        this.base = base;
        this.closeness = closeness;
        this.overlap = overlap;
    }

    public Edge base()
    {
        return this.base;
    }

    public Percent closeness()
    {
        return this.closeness;
    }

    public Edge enhancing()
    {
        return this.enhancing;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Conflation)
        {
            final var that = (Conflation) object;
            return this.base.equals(that.base);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.base.hashCode();
    }

    public boolean isCloserThan(final Conflation that)
    {
        return that == null || closeness().isGreaterThan(that.closeness());
    }

    public boolean isCloserThan(final Percent closeness)
    {
        return closeness().isGreaterThan(closeness);
    }

    public Distance overlap()
    {
        return this.overlap;
    }

    @Override
    public String toString()
    {
        return "[Conflation enhancing = " + edge(enhancing()) + ", base = " + edge(base()) + ", closeness = "
                + this.closeness + "]";
    }

    private String edge(final Edge edge)
    {
        return edge.identifier() + " (" + edge.displayRoadName() + ")";
    }
}
