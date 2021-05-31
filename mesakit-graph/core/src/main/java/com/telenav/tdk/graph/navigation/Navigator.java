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

package com.telenav.kivakit.graph.navigation;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.io.load.loaders.decimation.DecimatingGraphLoader;
import com.telenav.kivakit.graph.io.load.loaders.decimation.DecimationNavigator;
import com.telenav.kivakit.graph.navigation.navigators.*;

/**
 * A navigator is used to explore the road network from a given edge. A {@link Navigator} is used to decide what edge is
 * next with {@link #next(Edge, Direction)} where the direction is either {@link Direction#IN} or {@link Direction#OUT}.
 * This method uses the subclass implementation of {@link #in(Edge)} and {@link #out(Edge)} to select the next edge in
 * the given direction. Several navigators are provided:
 * <p>
 * <b>Navigators</b>
 * <ul>
 *     <li>{@link NonBranchingNavigator} - Returns in and out edges so long as there are no branches. Note that this includes
 *                                         the u-turn option on any two way street. If you don't want to consider u-turns on a
 *                                         two-way street as "branches", try using {@link NonBranchingNoUTurnNavigator}.</li>
 *     <li>{@link NonBranchingNoUTurnNavigator} - Returns in and out edges so long as there are no branches. Does not include u-turns</li>
 *     <li>{@link WayNavigator} - Navigates in both directions so long as the way identifier is the same as the start edge.
 *                                This navigator can be used to piece together a way from the edges that came from it.</li>
 *     <li>{@link DecimationNavigator} - Used by the {@link DecimatingGraphLoader} to connect together small edges that don't
 *                                       turn too sharply to reduce the complexity of rendering edges when zoomed out</li>
 *     <li>{@link NamedRoadNavigator} - Navigates a section of road with the same name, no branches, no u-turns and no loops.</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see Direction
 * @see WayNavigator
 * @see DecimationNavigator
 * @see NamedRoadNavigator
 * @see NonBranchingNavigator
 * @see NonBranchingNoUTurnNavigator
 */
@SuppressWarnings({ "StaticInitializerReferencesSubClass" })
public abstract class Navigator
{
    /**
     * A navigator that doesn't allow any branching at all (including u-turns on two-way road edges!). This effectively
     * means only edges on a one-way road that are directly connected without an intersection.
     */
    public static final Navigator NON_BRANCHING = new NonBranchingNavigator();

    /**
     * A navigator that doesn't allow branching, but doesn't consider the omnipresent u-turn possibility on a two way
     * road as a branch.
     */
    public static final Navigator NON_BRANCHING_NO_UTURN = new NonBranchingNoUTurnNavigator();

    /**
     * NON_BRANCHING_NO_UTURN navigator that doesn't consider dead end spurs
     */
    public static Navigator NON_BRANCHING_WITH_SPURS_NO_UTURN = new NonBranchingWithSpursNoUTurnNavigator();

    /**
     * A navigator that doesn't allow any bifurcations (either merging or branching) or u-turns
     */
    public static Navigator NON_BRANCHING_NO_MERGE_NO_UTURN = new NonBranchingNoMergeNoUTurnNavigator();

    /**
     * A navigator that doesn't allow any bifurcations (either merging or branching), u-turns or loops
     */
    public static final Navigator NON_BRANCHING_NO_MERGE_NO_UTURN_NO_LOOP = new NonBranchingNoMergeNoUTurnNoLoopNavigator();

    /**
     * The direction to navigate in, either "in" (backwards) or "out" (forwards).
     */
    public enum Direction
    {
        IN,
        OUT;

        public boolean isIn()
        {
            return this == IN;
        }

        public boolean isOut()
        {
            return this == OUT;
        }

        public Direction reverse()
        {
            if (isIn())
            {
                return OUT;
            }
            else
            {
                return IN;
            }
        }
    }

    /**
     * @return The previous edge before the given edge, or null if no previous exists
     */
    public abstract Edge in(Edge edge);

    /**
     * @return The next edge in the given direction
     */
    public Edge next(final Edge edge, final Direction direction)
    {
        return direction == Direction.IN ? in(edge) : out(edge);
    }

    /**
     * @return The next edge after the given edge, or null if no next exists
     */
    public abstract Edge out(Edge edge);
}
