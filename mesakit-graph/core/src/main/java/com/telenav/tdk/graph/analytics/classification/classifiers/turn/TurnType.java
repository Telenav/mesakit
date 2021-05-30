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

package com.telenav.tdk.graph.analytics.classification.classifiers.turn;

public enum TurnType
{
    LEFT
            {
                @java.lang.Override
                @Override
                public boolean isLeft()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isLeftSideTurn()
                {
                    return true;
                }
            },
    SLIGHT_LEFT
            {
                @java.lang.Override
                @Override
                public boolean isLeft()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isLeftSideTurn()
                {
                    return true;
                }
            },
    HARD_LEFT
            {
                @java.lang.Override
                @Override
                public boolean isLeft()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isLeftSideTurn()
                {
                    return true;
                }
            },
    RIGHT
            {
                @java.lang.Override
                @Override
                public boolean isRight()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isRightSideTurn()
                {
                    return true;
                }
            },
    SLIGHT_RIGHT
            {
                @java.lang.Override
                @Override
                public boolean isRight()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isRightSideTurn()
                {
                    return true;
                }
            },
    HARD_RIGHT
            {
                @java.lang.Override
                @Override
                public boolean isRight()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isRightSideTurn()
                {
                    return true;
                }
            },
    LEFT_SIDE_STRAIGHT_ON
            {
                @java.lang.Override
                @Override
                public boolean isLeftSideTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isStraightOn()
                {
                    return true;
                }
            },
    RIGHT_SIDE_STRAIGHT_ON
            {
                @java.lang.Override
                @Override
                public boolean isRightSideTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isStraightOn()
                {
                    return true;
                }
            },
    LEFT_SIDE_U_TURN
            {
                @java.lang.Override
                @Override
                public boolean isLeftSideTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isUTurn()
                {
                    return true;
                }
            },
    RIGHT_SIDE_U_TURN
            {
                @java.lang.Override
                @Override
                public boolean isRightSideTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isUTurn()
                {
                    return true;
                }
            },
    IN_PLACE_U_TURN
            {
                @java.lang.Override
                @Override
                public boolean isUTurn()
                {
                    return true;
                }
            },

    SHARP_U_TURN
            {
                @java.lang.Override
                @Override
                public boolean isUTurn()
                {
                    return true;
                }
            },

    LOOP
            {
                @java.lang.Override
                @Override
                public boolean isUTurn()
                {
                    return true;
                }
            },

    ZIGZAG_LEFT
            {
                @java.lang.Override
                @Override
                public boolean isLeftSideTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isZigzag()
                {
                    return true;
                }
            },

    ZIGZAG_RIGHT
            {
                @java.lang.Override
                @Override
                public boolean isRightSideTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isZigzag()
                {
                    return true;
                }
            },

    ZIGZAG_U_TURN
            {
                @java.lang.Override
                @Override
                public boolean isUTurn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isZigzag()
                {
                    return true;
                }
            },

    ZIGZAG_STRAIGHT_ON
            {
                @java.lang.Override
                @Override
                public boolean isStraightOn()
                {
                    return true;
                }

                @java.lang.Override
                @Override
                public boolean isZigzag()
                {
                    return true;
                }
            };

    public boolean isLeft()
    {
        return false;
    }

    /**
     * @return true if the turn type is one of the followings: LEFT, SLIGHT_LEFT, HARD_LEFT, LEFT_SIDE_STRAIGHT_ON and
     * LEFT_SIDE_U_TURN
     */
    public boolean isLeftSideTurn()
    {
        return false;
    }

    public boolean isRight()
    {
        return false;
    }

    /**
     * @return true if the turn type is one of the followings: RIGHT, SLIGHT_RIGHT, HARD_RIGHT, RIGHT_SIDE_STRAIGHT_ON
     * and RIGHT_SIDE_U_TURN
     */
    public boolean isRightSideTurn()
    {
        return false;
    }

    public boolean isStraightOn()
    {
        return false;
    }

    public boolean isUTurn()
    {
        return false;
    }

    /**
     * @return if this turn contains both obvious left and right turns
     */
    public boolean isZigzag()
    {
        return false;
    }
}
