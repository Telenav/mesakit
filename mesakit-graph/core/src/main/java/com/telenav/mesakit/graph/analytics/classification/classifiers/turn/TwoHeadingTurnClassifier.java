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

import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.mesakit.map.measurements.geographic.Headed;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * Classifies two headings as a turn type
 *
 * @author jonathanl (shibo)
 */
public final class TwoHeadingTurnClassifier
{
    public static final TwoHeadingTurnClassifier DEFAULT = new TwoHeadingTurnClassifier();

    private Angle normalTurn = Angle.degrees(55);

    private Angle hardTurn = Angle.degrees(125);

    private Angle uTurn = Angle.degrees(160);

    private Angle slightTurn = Angle.degrees(16);

    public void hardTurn(Angle hardTurn)
    {
        this.hardTurn = hardTurn;
    }

    public void normalTurn(Angle normalTurn)
    {
        this.normalTurn = normalTurn;
    }

    public void slightTurn(Angle slightTurn)
    {
        this.slightTurn = slightTurn;
    }

    public TurnType type(Headed from, Headed to)
    {
        var fromHeading = from.heading();
        var toHeading = to.heading();

        var clockwise = fromHeading.difference(toHeading, Chirality.CLOCKWISE);
        if (clockwise.isClose(Angle._180_DEGREES, Angle.degrees(1)))
        {
            return TurnType.IN_PLACE_U_TURN;
        }
        if (clockwise.isLessThan(Angle._180_DEGREES))
        {
            if (clockwise.isGreaterThanOrEqualTo(Angle._0_DEGREES) && clockwise.isLessThan(slightTurn))
            {
                return TurnType.RIGHT_SIDE_STRAIGHT_ON;
            }
            if (clockwise.isGreaterThanOrEqualTo(slightTurn) && clockwise.isLessThan(normalTurn))
            {
                return TurnType.SLIGHT_RIGHT;
            }
            if (clockwise.isGreaterThanOrEqualTo(normalTurn) && clockwise.isLessThan(hardTurn))
            {
                return TurnType.RIGHT;
            }
            if (clockwise.isGreaterThanOrEqualTo(hardTurn) && clockwise.isLessThan(uTurn))
            {
                return TurnType.HARD_RIGHT;
            }
            if (clockwise.isGreaterThanOrEqualTo(uTurn) && clockwise.isLessThan(Angle._180_DEGREES))
            {
                return TurnType.RIGHT_SIDE_U_TURN;
            }
        }
        var counterclockwise = fromHeading.difference(toHeading, Chirality.COUNTERCLOCKWISE);
        if (counterclockwise.isLessThan(Angle._180_DEGREES))
        {
            if (counterclockwise.isGreaterThanOrEqualTo(Angle._0_DEGREES) && counterclockwise.isLessThan(slightTurn))
            {
                return TurnType.LEFT_SIDE_STRAIGHT_ON;
            }
            if (counterclockwise.isGreaterThanOrEqualTo(slightTurn)
                    && counterclockwise.isLessThan(normalTurn))
            {
                return TurnType.SLIGHT_LEFT;
            }
            if (counterclockwise.isGreaterThanOrEqualTo(normalTurn) && counterclockwise.isLessThan(hardTurn))
            {
                return TurnType.LEFT;
            }
            if (counterclockwise.isGreaterThanOrEqualTo(hardTurn) && counterclockwise.isLessThan(uTurn))
            {
                return TurnType.HARD_LEFT;
            }
            if (counterclockwise.isGreaterThanOrEqualTo(uTurn)
                    && counterclockwise.isLessThan(Angle._180_DEGREES))
            {
                return TurnType.LEFT_SIDE_U_TURN;
            }
        }
        return fail("Internal Error");
    }

    public void uTurn(Angle uTurn)
    {
        this.uTurn = uTurn;
    }
}
