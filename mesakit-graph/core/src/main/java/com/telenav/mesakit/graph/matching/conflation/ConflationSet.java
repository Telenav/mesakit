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

import com.telenav.kivakit.language.level.Percent;
import com.telenav.mesakit.graph.Edge;

import java.util.HashSet;

public class ConflationSet extends HashSet<Conflation>
{
    private static final long serialVersionUID = 5435013170660624582L;

    public ConflationSet()
    {
    }

    public ConflationSet(ConflationSet conflations)
    {
        addAll(conflations);
    }

    /**
     * Adds the given new conflation object to the set of conflations, but ensures that conflations that are end-to-end
     * don't compete with each other. Instead, the best (closest) match is chosen.
     */
    public void addUnambiguously(Conflation newConflation)
    {
        // Go through each conflation
        var connected = false;
        for (var conflation : new ConflationSet(this))
        {
            // and if the existing conflation is connected to the new conflation
            if (conflation.base().isConnectedTo(newConflation.base()))
            {
                // set flag that says the edges were connected
                connected = true;

                // and the new conflation is a better match,
                if (newConflation.closeness().isGreaterThan(conflation.closeness()))
                {
                    // then replace the existing conflation with the new conflation
                    remove(conflation);
                    add(newConflation);
                }
            }
        }

        // There was no connected conflation
        if (!connected)
        {
            // so add the new conflation
            add(newConflation);
        }
    }

    public ConflationSet closerThan(Percent closeness)
    {
        var conflations = new ConflationSet();
        for (var conflation : this)
        {
            if (conflation.isCloserThan(closeness))
            {
                conflations.add(conflation);
            }
        }
        return conflations;
    }

    public Conflation closest()
    {
        Conflation closest = null;
        for (var conflation : this)
        {
            if (conflation.isCloserThan(closest))
            {
                closest = conflation;
            }
        }
        return closest;
    }

    public Conflation forBaseEdge(Edge base)
    {
        for (var conflation : this)
        {
            if (conflation.base().equals(base))
            {
                return conflation;
            }
        }
        return null;
    }

    public boolean isAmbiguous()
    {
        return size() > 1;
    }
}
