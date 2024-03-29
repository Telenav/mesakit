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

package com.telenav.mesakit.graph.relations.restrictions.classifiers;

import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TurnClassifier;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TurnType;
import com.telenav.mesakit.map.measurements.geographic.Angle;

import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import static com.telenav.mesakit.map.measurements.geographic.Angle.degrees;

/**
 * A legacy turn classifier used in the OSM++ new probe processor project for classifying turn restrictions.
 * <p>
 * Classifies turns as left, right, straight-on, in-place-u-turn, u-turn or loop based on the final heading of first
 * edge in route and the initial heading of last edge in route.
 *
 * @author jonathanl (shibo)
 */
public class TurnRestrictionsTurnClassifier implements TurnClassifier
{
    public static final Angle MINIMUM_UTURN_ANGLE = degrees(170);

    @Override
    public TurnType type(Route route)
    {
        if (route.isInPlaceUTurn())
        {
            return TurnType.IN_PLACE_U_TURN;
        }

        // If the route forms a loop of some kind
        if (route.isLoop())
        {
            // it's a loop turn
            return TurnType.LOOP;
        }

        // Get initial and final headings of the turn route
        var initialHeading = route.first().finalHeading();
        var finalHeading = route.last().initialHeading();

        // Find out how much we're going right
        var right = initialHeading.difference(finalHeading, Chirality.CLOCKWISE);

        // If we're bearing right between 0 and 40 degrees we will consider that straight-on
        if (right.isGreaterThanOrEqualTo(degrees(0)) && right.isLessThanOrEqualTo(degrees(40)))
        {
            return TurnType.RIGHT_SIDE_STRAIGHT_ON;
        }

        // If we're turning right between 40 and 135 degrees, it's a right turn
        if (right.isGreaterThanOrEqualTo(degrees(40)) && right.isLessThanOrEqualTo(degrees(135)))
        {
            return TurnType.RIGHT;
        }

        // Find out how much we're going left
        var left = initialHeading.difference(finalHeading, Chirality.COUNTERCLOCKWISE);

        // If we're bearing left between 0 and 40 degrees we will consider that straight-on
        if (left.isGreaterThanOrEqualTo(degrees(0)) && left.isLessThanOrEqualTo(degrees(40)))
        {
            return TurnType.LEFT_SIDE_STRAIGHT_ON;
        }

        // If we're turning left between 40 and 135 degrees, it's a right turn
        if (left.isGreaterThanOrEqualTo(degrees(40)) && left.isLessThanOrEqualTo(degrees(135)))
        {
            return TurnType.LEFT;
        }

        if (route.isSharpUTurn(MINIMUM_UTURN_ANGLE))
        {
            return TurnType.SHARP_U_TURN;
        }

        // We're not turning left or right and we're not going straight, so it must be a u-turn
        return TurnType.LEFT_SIDE_U_TURN;
    }
}
