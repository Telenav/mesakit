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

package com.telenav.tdk.graph.relations.restrictions;

import com.telenav.tdk.graph.EdgeRelation;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.analytics.classification.classifiers.turn.TwoHeadingTurnClassifier;
import com.telenav.tdk.graph.collections.EdgeSet;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.measurements.Heading;

import java.util.List;

public class TurnRestriction
{
    public enum Prohibition
    {
        NO,
        ONLY
    }

    public enum Type
    {
        NO_LEFT(Prohibition.NO),
        NO_RIGHT(Prohibition.NO),
        NO_U(Prohibition.NO),
        NO_STRAIGHT_ON(Prohibition.NO),
        ONLY_LEFT(Prohibition.ONLY),
        ONLY_RIGHT(Prohibition.ONLY),
        ONLY_STRAIGHT_ON(Prohibition.ONLY);

        public static Type forEdgeRelation(final EdgeRelation relation)
        {
            String value = null;
            final var restriction = relation.tagValue("restriction");
            if (restriction != null)
            {
                value = restriction;
            }
            else
            {
                final var conditional = relation.tagValue("restriction:conditional");
                if (conditional != null)
                {
                    value = conditional;
                }
            }
            return forTagValue(value);
        }

        public static Type forHeadings(final Prohibition prohibition, final Heading in, final Heading out)
        {
            final var type = TwoHeadingTurnClassifier.DEFAULT.type(in, out);
            if (type != null)
            {
                switch (type)
                {
                    case LEFT:
                    case SLIGHT_LEFT:
                    case HARD_LEFT:
                    case ZIGZAG_LEFT:
                        return prohibition == Prohibition.NO ? NO_LEFT : ONLY_LEFT;

                    case LEFT_SIDE_U_TURN:
                    case IN_PLACE_U_TURN:
                    case RIGHT_SIDE_U_TURN:
                    case SHARP_U_TURN:
                    case ZIGZAG_U_TURN:
                        return NO_U;

                    case LEFT_SIDE_STRAIGHT_ON:
                    case RIGHT_SIDE_STRAIGHT_ON:
                    case ZIGZAG_STRAIGHT_ON:
                        return prohibition == Prohibition.NO ? NO_STRAIGHT_ON : ONLY_STRAIGHT_ON;

                    case RIGHT:
                    case SLIGHT_RIGHT:
                    case HARD_RIGHT:
                    case ZIGZAG_RIGHT:
                        return prohibition == Prohibition.NO ? NO_RIGHT : ONLY_RIGHT;

                    case LOOP:
                    default:
                        throw new IllegalStateException();
                }
            }
            return null;
        }

        public static Type forTagValue(String value)
        {
            if (value != null)
            {
                if (value.contains("@"))
                {
                    value = value.substring(0, value.indexOf("@"));
                }
                switch (value.trim())
                {
                    case "no_left_turn":
                        return NO_LEFT;

                    case "no_right_turn":
                        return NO_RIGHT;

                    case "no_u_turn":
                        return NO_U;

                    case "no_straight_on":
                        return NO_STRAIGHT_ON;

                    case "only_left_turn":
                        return ONLY_LEFT;

                    case "only_right_turn":
                        return ONLY_RIGHT;

                    case "only_straight_on":
                        return ONLY_STRAIGHT_ON;
                }
            }
            return null;
        }

        private final Prohibition prohibition;

        Type(final Prohibition prohibition)
        {
            this.prohibition = prohibition;
        }

        public boolean isNo()
        {
            return prohibition() == Prohibition.NO;
        }

        public boolean isOnly()
        {
            return prohibition() == Prohibition.ONLY;
        }

        public Prohibition prohibition()
        {
            return prohibition;
        }
    }

    private final Type type;

    private final Route from;

    private final Route via;

    private final Route to;

    public TurnRestriction(final EdgeRelation relation, final Route from, final Route via, final Route to)
    {
        type = Type.forEdgeRelation(relation);
        this.from = from;
        this.via = via;
        this.to = to;
    }

    public Route from()
    {
        return from;
    }

    public Heading in()
    {
        return from.last().finalHeading();
    }

    public boolean isBad()
    {
        return route() == null;
    }

    public Location location()
    {
        return from != null ? from.last().toLocation() : null;
    }

    public Heading out()
    {
        return to.first().initialHeading();
    }

    public Route route()
    {
        if (from != null && to != null)
        {
            if (via == null)
            {
                if (from.canAppend(to))
                {
                    return from.append(to);
                }
            }
            else
            {
                if (from.canAppend(via) && via.canAppend(to))
                {
                    return from.append(via).append(to);
                }
            }
        }
        return null;
    }

    public List<Route> routes()
    {
        final var edges = new EdgeSet();
        edges.add(from);
        edges.add(to);
        edges.add(via);
        return edges.asRoutes();
    }

    public Route to()
    {
        return to;
    }

    @Override
    public String toString()
    {
        return "[TurnRestriction type = " + type()
                + ", from = " + from()
                + ", via = " + via()
                + ", to = " + to()
                + ", route = " + route() + "]";
    }

    public Type type()
    {
        return type;
    }

    public Route via()
    {
        return via;
    }
}
