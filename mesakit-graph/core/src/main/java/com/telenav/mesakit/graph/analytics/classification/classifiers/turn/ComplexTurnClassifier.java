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

package com.telenav.mesakit.graph.analytics.classification.classifiers.turn;

import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * Classifies complex turns
 *
 * @author jonathanl (shibo)
 */
public class ComplexTurnClassifier implements TurnClassifier
{
    public static final ComplexTurnClassifier DEFAULT = new ComplexTurnClassifier(Configuration.DEFAULT);

    public static class Configuration
    {
        public static final Configuration DEFAULT = new Configuration(TwoHeadingTurnClassifier.DEFAULT,
                Angle.degrees(16));

        private TwoHeadingTurnClassifier twoHeadingClassifier;

        private Angle straight;

        public Configuration()
        {
        }

        public Configuration(TwoHeadingTurnClassifier twoHeadingClassifier, Angle straight)
        {
            this.twoHeadingClassifier = twoHeadingClassifier;
            this.straight = straight;
        }

        public Angle straight()
        {
            return straight;
        }

        public void straight(Angle straight)
        {
            this.straight = straight;
        }

        public TwoHeadingTurnClassifier twoHeadingClassifier()
        {
            return twoHeadingClassifier;
        }

        public void twoHeadingClassifier(TwoHeadingTurnClassifier twoHeadingClassifier)
        {
            this.twoHeadingClassifier = twoHeadingClassifier;
        }
    }

    private final Configuration configuration;

    public ComplexTurnClassifier(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public TurnType type(Route route)
    {
        // If the route forms a loop,
        if (route.isLoop())
        {
            // return a loop turn type
            return TurnType.LOOP;
        }

        // Get the first and last edge
        var first = route.first();
        var last = route.last();

        // If there are only two edges and the first is the last edge reversed,
        if (route.size() == 2 && first.equals(last.reversed()))
        {
            // then it's a simple in-place u-turn
            return TurnType.IN_PLACE_U_TURN;
        }

        // Get two heading classifier to determine turn type
        var twoHeadingClassifier = configuration.twoHeadingClassifier();

        // If we have a two-edge route
        if (route.size() == 2)
        {
            // and if both edges are roughly straight,
            var firstRoadShape = first.roadShape();
            var lastRoadShape = last.roadShape();
            if (firstRoadShape.isStraight(Distance.MAXIMUM, configuration.straight())
                    && lastRoadShape.isStraight(Distance.MAXIMUM, configuration.straight()))
            {
                // use the two-heading turn classifier to determine the turn type
                return twoHeadingClassifier.type(first.asSegment(), last.asSegment());
            }
            else
            {
                // Classify the turn using the last segment of the first edge and the first segment
                // of the last edge
                return twoHeadingClassifier.type(firstRoadShape.lastSegment(), lastRoadShape.firstSegment());
            }
        }
        else
        {
            // For route with more than two edges, just classify the turn using the last segment of
            // the first edge and the first segment of the last edge
            var type = twoHeadingClassifier.type(first.lastSegment(), last.firstSegment());

            // If we got back a u-turn, it may not be classified right because it's not a two-edge
            // turn.
            // so we can't just use the result from the two-heading classifier.
            if (type.isUTurn())
            {
                // Instead, we look at how the route bends and return left or right based on that
                return route.polyline().bend().isLessThan(Angle._0_DEGREES)
                        ? TurnType.LEFT_SIDE_U_TURN
                        : TurnType.RIGHT_SIDE_U_TURN;
            }
            return type;
        }
    }
}
