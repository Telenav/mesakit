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

package com.telenav.mesakit.map.measurements.geographic;

import com.telenav.mesakit.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementGeographic;
import com.telenav.kivakit.core.collections.map.CaseFoldingStringMap;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.interfaces.naming.Named;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.regex.Pattern;

/**
 * The directions on a compass, with standard (English only) abbreviations such as "SW" or "W". These abbreviations, as
 * well as full names like "North" and highway directions such as "SOUTHBOUND" or "S.W." can be parsed with {@link
 * #parse(String)}. A direction can be converted to a heading with {@link #asHeading()}.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public enum Direction implements Named
{
    EAST("E", Heading.EAST),
    NORTH("N", Heading.NORTH),
    NORTHEAST("NE", Heading.NORTHEAST),
    NORTHWEST("NW", Heading.NORTHWEST),
    SOUTH("S", Heading.SOUTH),
    SOUTHEAST("SE", Heading.SOUTHEAST),
    SOUTHWEST("SW", Heading.SOUTHWEST),
    WEST("W", Heading.WEST);

    private static final CaseFoldingStringMap<Direction> nameToDirection = new CaseFoldingStringMap<Direction>();

    private static final Pattern SPACES_AND_PERIODS = Pattern.compile("[\\s.]");

    static
    {
        // Populate the nameToDirection map with canonical names that    we can parse
        for (final var direction : values())
        {
            // NORTH
            Direction.nameToDirection.put(direction.name(), direction);

            // N
            Direction.nameToDirection.put(direction.abbreviation(), direction);

            if (direction.abbreviation().length() == 1)
            {
                // NB
                Direction.nameToDirection.put(direction.abbreviation() + "B", direction);

                // NORTHBOUND
                Direction.nameToDirection.put(direction.name() + "BOUND", direction);
            }
        }
    }

    /**
     * @param text The text to be parsed(NW, N, SB, SE, SOUTHBOUND, EB, etc.)
     * @return The direction associated with the identifier or null if none could be found.
     */
    public static Direction parse(final String text)
    {
        return nameToDirection.get(SPACES_AND_PERIODS.matcher(text).replaceAll(""));
    }

    /**
     * Converts to and from a {@link Direction}
     */
    public static class Converter extends BaseStringConverter<Direction>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Direction onConvertToObject(final String value)
        {
            return parse(value);
        }
    }

    /** Abbreviation like "N" or "SW" */
    private final String abbreviation;

    /** The heading for this compass direction */
    private final Heading heading;

    Direction(final String abbreviation, final Heading heading)
    {
        this.abbreviation = abbreviation;
        this.heading = heading;
    }

    /**
     * @return The standard abbreviation for this direction in English, such as "N" or "SW"
     */
    public String abbreviation()
    {
        return abbreviation;
    }

    /**
     * @return This direction as a {@link Heading}
     */
    public Heading asHeading()
    {
        return heading;
    }

    @Override
    public String toString()
    {
        return abbreviation;
    }
}