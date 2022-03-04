////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.interfaces.model.Identifiable;
import com.telenav.kivakit.core.string.CaseFormat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Types of highways, having identifiers that can be accessed with {@link #identifier()} and {@link
 * #forIdentifier(long)}.
 *
 * @author jonathanl (shibo)
 */
public enum HighwayType implements Identifiable
{
    MOTORWAY(0),
    MOTORWAY_LINK(1),
    TRUNK(2),
    TRUNK_LINK(3),
    PRIMARY(4),
    PRIMARY_LINK(5),
    SECONDARY(6),
    SECONDARY_LINK(7),
    TERTIARY(8),
    TERTIARY_LINK(9),
    RESIDENTIAL(10),
    RESIDENTIAL_LINK(11),
    UNCLASSIFIED(12),
    SERVICE(13),
    REST_AREA(14),
    SERVICES(15),
    ROAD(16),
    TRACK(17),
    UNDEFINED(18),
    UNKNOWN(19),
    LIVING_STREET(20),
    PRIVATE(21),
    DRIVEWAY(22),
    FOOTWAY(23),
    PEDESTRIAN(24),
    STEPS(25),
    BRIDLEWAY(26),
    CONSTRUCTION(27),
    PATH(28),
    CYCLEWAY(29),
    BUS_GUIDEWAY(30),
    MINOR(31),
    TURNING_CIRCLE(32),
    BYWAY(33),
    UNSURFACED(34),
    PLATFORM(35),
    ABANDONED(36),
    RAZED(37),
    RACEWAY(38),
    PLANNED(39),
    PROPOSED(40),
    PROPOSAL(41),
    HISTORIC(42),
    ESCALATOR(43),
    ELEVATOR(44),
    DISMANTLED(45),
    DISUSED(46),
    BUS_STOP(47),
    HALLWAY(48),
    FORD(49),
    CONVEYOR(50),
    CROSSING(51),
    PUBLIC_TRANSPORT(52),
    TRAIL(53),
    CLOSED(54),
    WALKWAY(55),
    OLD(56),
    STREET_LAMP(57),
    STEPPING_STONES(58),
    KERB(59),
    LAYBY(60),
    GIVE_WAY(61),
    ESCAPE(62),
    GALLOP(63),
    PASSING_PLACE(64),
    TOWPATH(65),
    SIDEWALK(66),
    CORRIDOR(67),
    ACCESS(68),
    NO(69),
    DEPOT(70),
    VERGE(71),
    MINI_ROUNDABOUT(72),
    MOTORWAY_JUNCTION(73),
    NULL(Byte.MIN_VALUE);

    private static final Map<Long, HighwayType> identifierToType = new HashMap<>();

    static
    {
        Arrays.stream(values()).forEach(type -> identifierToType.put(type.identifier(), type));
    }

    public static HighwayType forIdentifier(long identifier)
    {
        return identifierToType.get(identifier);
    }

    public static HighwayType forName(String name)
    {
        try
        {
            return valueOf(name);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }
    }

    private final int identifier;

    HighwayType(int identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public long identifier()
    {
        return identifier;
    }

    public String value()
    {
        return CaseFormat.upperUnderscoreToLowerHyphen(name());
    }
}
