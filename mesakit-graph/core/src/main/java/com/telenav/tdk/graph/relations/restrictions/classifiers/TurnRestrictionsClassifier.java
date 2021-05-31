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

package com.telenav.kivakit.graph.relations.restrictions.classifiers;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.analytics.classification.EdgeClassifier;

/**
 * Created by udayg on 4/15/16.
 */
public class TurnRestrictionsClassifier implements EdgeClassifier
{
    public static TurnRestrictionsClassifier INSTANCE = new TurnRestrictionsClassifier();

    @Override
    public boolean accept(final Edge edge)
    {
        return count(edge) > 0;
    }

    private int count(final Edge edge)
    {
        var turnRestrictionCount = edge.turnRestrictionsBeginningAt().size();
        if (edge.isTwoWay())
        {
            turnRestrictionCount += edge.reversed().turnRestrictionsBeginningAt().size();
        }
        return turnRestrictionCount;
    }
}
