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

package com.telenav.mesakit.navigation.routing;

import com.telenav.mesakit.graph.Edge;

/**
 * Level promotion strategy to be used for determining what edges should be explored when routing
 *
 * @author xunliu
 * @author jonathanl (shibo)
 */
public interface LevelPromoter
{
    LevelPromoter NULL = new LevelPromoter()
    {
        @Override
        public void onRelax(final Edge edge)
        {
        }

        @Override
        public void onSettle(final Edge edge)
        {
        }

        @Override
        public boolean shouldExplore(final Edge edge)
        {
            return true;
        }
    };

    void onRelax(final Edge edge);

    void onSettle(final Edge edge);

    /**
     * @return True if the given edge should be explored
     */
    boolean shouldExplore(final Edge edge);
}
