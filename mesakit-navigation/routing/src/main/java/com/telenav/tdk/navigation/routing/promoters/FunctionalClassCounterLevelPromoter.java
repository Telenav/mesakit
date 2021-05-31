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

package com.telenav.kivakit.navigation.routing.promoters;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.map.road.model.RoadFunctionalClass;
import com.telenav.kivakit.navigation.routing.LevelPromoter;

public class FunctionalClassCounterLevelPromoter implements LevelPromoter
{
    private static final int[] promotionThreshold = { 0, 50, 40, 40, 40, 30 };

    private RoadFunctionalClass currentLevel = RoadFunctionalClass.MINIMUM;

    private final int[] counter = { 0, 0, 0, 0, 0, 0 };

    @Override
    public void onRelax(final Edge edge)
    {
        // Increase the counter for the given road functional class
        this.counter[edge.roadFunctionalClass().identifier()]++;

        // If there is a next level
        var nextLevel = this.currentLevel.nextLevel();
        if (nextLevel != null)
        {
            // Loop through levels getting the sum of all counters
            var total = 0;
            for (; nextLevel != null; nextLevel = nextLevel.nextLevel())
            {
                total += this.counter[nextLevel.identifier()];
            }

            // If we reached the promotion threshold,
            if (total >= promotionThreshold[this.currentLevel.identifier()])
            {
                // promote to next level
                this.currentLevel = this.currentLevel.nextLevel();
            }
        }
    }

    @Override
    public void onSettle(final Edge edge)
    {
        if (edge != null)
        {
            // Decrease the counter based on edge's functional class, but not less than zero
            final var index = edge.roadFunctionalClass().identifier();
            this.counter[index] = Math.max(0, this.counter[index] - 1);
        }
    }

    @Override
    public boolean shouldExplore(final Edge edge)
    {
        // We should settle the edge if the road functional class is NOT less than the current level
        return !edge.roadFunctionalClass().isLessImportantThan(this.currentLevel);
    }
}
