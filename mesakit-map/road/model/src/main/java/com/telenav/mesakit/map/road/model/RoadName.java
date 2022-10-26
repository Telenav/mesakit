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

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.string.StringSimilarity;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.level.Confidence;
import com.telenav.kivakit.core.value.name.Name;
import com.telenav.kivakit.extraction.Extractor;
import com.telenav.mesakit.map.measurements.geographic.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Representation of the name of a road.
 *
 * @author jonathanl (shibo)
 */
public class RoadName extends Name
{
    /**
     * An empty road name
     */
    public static RoadName NULL = forName("");

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private static final Pattern OCTANT_PATTERN = Pattern.compile(
            "\\b(N|S|E|W|NORTH|SOUTH|EAST|WEST|NW|NE|SW|SE|NORTHWEST|NORTHEAST|SOUTHWEST|SOUTHEAST)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DIRECTION_PATTERN = Pattern.compile(
            "\\b([NSEW])(B)?|(NORTH|SOUTH|EAST|WEST)(BOUND)?|(NW|NE|SW|SE|NORTHWEST|NORTHEAST|SOUTHWEST|SOUTHEAST)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern HIGHWAY_IDENTIFIERS = Pattern
            .compile("\\bRT\\b|\\b?+I\\b++|ON RAMP|OFF RAMP|HWY|(.*?)-");

    public static RoadName forName(String name)
    {
        if (Strings.isNullOrBlank(name))
        {
            return null;
        }
        return new RoadName(name);
    }

    public static RoadName forName(String nameOnly, Direction direction)
    {
        return forName(nameOnly + " " + direction);
    }

    public enum Type
    {
        OFFICIAL,
        ALTERNATE,
        ROUTE,
        EXIT
    }

    private RoadName(String name)
    {
        super(name);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof RoadName that)
        {
            return name().equalsIgnoreCase(that.name());
        }
        return false;
    }

    public <T> T extract(Extractor<T, RoadName> extractor)
    {
        return extractor.extract(this);
    }

    /**
     * Returns the direction the road is headed. For example, "I-5 Northbound" will return Direction.NORTH.
     */
    public Direction extractDirection()
    {
        return extractDirection(DIRECTION_PATTERN);
    }

    /**
     * Returns this road name without any direction or octant information. For example, "West Greenlake Way North"
     * becomes "Greenlake Way"
     */
    @SuppressWarnings("SpellCheckingInspection")
    public RoadName extractNameOnly()
    {
        return withoutDirection();
    }

    /**
     * Returns the octant (quadrant but including all 8 directions) of a city that a road is in, for example "North 1st
     * Street" or "Westlake Avenue North"
     */
    @SuppressWarnings("SpellCheckingInspection")
    public Direction extractOctant()
    {
        return extractDirection(OCTANT_PATTERN);
    }

    @Override
    public int hashCode()
    {
        return name().toLowerCase().hashCode();
    }

    public Confidence matchConfidence(RoadName that, Direction thatDirection)
    {
        List<Direction> thatDirections = new ArrayList<>();
        if (thatDirection != null)
        {
            thatDirections.add(thatDirection);
        }
        return matchConfidence(that, thatDirections);
    }

    public Confidence matchConfidence(RoadName that, List<Direction> thatDirections)
    {
        var thisName = extractNameOnly().withoutHighwayIdentifiers().name().toUpperCase();
        var thatStreetName = that.extractNameOnly().withoutHighwayIdentifiers().name().toUpperCase();
        var thisDirection = extractDirection();

        DEBUG.trace("Comparing for perfect match between '${debug} ${debug}' and '${debug} ${debug}'", thisName,
                thisDirection, thatStreetName, thatDirections);

        // When the direction attribute is passed by calling code, check for a direction match(we
        // can eliminate candidates immediately if the directions don't match), otherwise we needn't
        // to check the direction if is match.
        if (thisDirection != null && !isMatched(thisDirection, thatDirections))
        {
            return Confidence.LOW;
        }

        // Ensure to see if we have a perfect match with direction. If both of the direction are
        // unknown, or this direction matches to that expected directions, we have a perfect match
        // with direction
        var isDirectionPerfectMatch = (thisDirection == null
                && (thatDirections == null || thatDirections.isEmpty()))
                || (thisDirection != null && thatDirections != null && !thatDirections.isEmpty()
                && isMatched(thisDirection, thatDirections));
        // Ensure to see if we have a perfect match with name and direction.
        var isPerfectMatch = thatStreetName.equalsIgnoreCase(thisName) && isDirectionPerfectMatch;

        if (isPerfectMatch)
        {
            // Indicate perfection.
            return Confidence.FULL;
        }

        // Compute the difference between strings.
        var levenshteinDistance = StringSimilarity.levenshteinDistance(thatStreetName, thisName);

        // Compute error ratio from levenshtein distance
        var maximumLength = Math.max(thatStreetName.length(), thisName.length());

        // Finally, take care of the case where one of the directions was unknown and that was why
        // the directions 'matched'. If either one is null then it is really 50 / 50 that we have a
        // match, so divide by 1.5. This divisor is to keep the confidence above the result
        // threshold (MINIMUM_NAME_MATCHING_CONFIDENCE = 0.5, defined in MapFeatureExtractor)
        var directionDivisor = !isDirectionPerfectMatch ? 1.5 : 1;

        var confidence = Confidence.confidence(
                (1. - (double) levenshteinDistance / (double) maximumLength) / directionDivisor);

        // Add for cases like 'US 101' and '101', 'CA 85' and '85'
        if (!Strings.isNullOrBlank(thatStreetName) && !Strings.isNullOrBlank(thisName) && !thatStreetName.equalsIgnoreCase(thisName)
                && (thatStreetName.contains(thisName) || (thisName.contains(thatStreetName))))
        {
            var confidenceForSimilarNames = Confidence.confidence(0.8 / directionDivisor);
            if (confidence.isLessThan(confidenceForSimilarNames))
            {
                confidence = confidenceForSimilarNames;
            }
        }

        return confidence;
    }

    public RoadName normalize()
    {
        return new RoadName(name().replaceAll("\\*", ""));
    }

    public RoadName withoutDirection()
    {
        return without(DIRECTION_PATTERN);
    }

    /**
     * Returns a street name object stripped of the standard highway identifiers and descriptors.
     */
    public RoadName withoutHighwayIdentifiers()
    {
        if (name() != null)
        {
            var stripPrefixes = HIGHWAY_IDENTIFIERS.matcher(name()).replaceAll("");
            var idxToTrim = stripPrefixes.indexOf("BOTH");
            var strippedName = stripPrefixes;
            if (idxToTrim > 0)
            {
                strippedName = stripPrefixes.substring(0, idxToTrim - 1);
            }
            return new RoadName(strippedName.trim());
        }
        return this;
    }

    public RoadName withoutOctant()
    {
        return without(OCTANT_PATTERN);
    }

    private Direction extractDirection(Pattern pattern)
    {
        // Split the string into words
        var words = words();

        // If there is more than one word
        if (words.size() > 1)
        {
            // and the last word matches the pattern
            var matcher = pattern.matcher(words.last());
            if (matcher.matches())
            {
                // return the direction for that word
                return Direction.parse(words.last());
            }

            // If the first word matches the pattern,
            matcher = pattern.matcher(words.first());
            if (matcher.matches())
            {
                // return the direction for that word
                return Direction.parse(words.first());
            }
        }

        // Either there aren't enough words or nothing matched the pattern
        return null;
    }

    /**
     * Returns true if the heading difference between the two directions is no more than 45 degree, such as EAST and
     * NORTHEAST.
     */
    private boolean isCloseTo(Direction a, Direction b)
    {
        if (a != null && b != null)
        {
            return a.abbreviation().contains(b.abbreviation()) || b.abbreviation().contains(a.abbreviation());
        }
        else
        {
            return false;
        }
    }

    /**
     * Ensure if the direction is matched to one of the expected directions. If expected direction is unknown, we will
     * always return 'matched'
     *
     * @return true if matched
     */
    private boolean isMatched(Direction direction, List<Direction> expectedDirections)
    {
        if (expectedDirections != null && !expectedDirections.isEmpty())
        {
            for (var expectedDirection : expectedDirections)
            {
                if (isCloseTo(direction, expectedDirection))
                {
                    return true;
                }
            }
            return false;
        }
        else
        {
            // If expected direction is unknown, we will always return 'matched'
            return true;
        }
    }

    private RoadName without(Pattern pattern)
    {
        // Split into words
        var words = words();

        // If there is more than one word
        if (words.size() > 1)
        {
            // and the last word matches the pattern,
            var matcher = pattern.matcher(words.last());
            if (matcher.matches())
            {
                // return this road name without the last word
                words.removeLast();
                return new RoadName(words.join(" "));
            }

            // If the first word matches the pattern,
            matcher = pattern.matcher(words.first());
            if (matcher.matches())
            {
                words.remove(0);
                return new RoadName(words.join(" "));
            }
        }
        return this;
    }

    private StringList words()
    {
        var words = new StringList(RoadLimits.WORDS_PER_ROAD_NAME);
        var name = name();
        var word = new StringBuilder();
        var separator = false;
        for (var i = 0; i < name.length(); i++)
        {
            var c = name.charAt(i);
            switch (c)
            {
                case '(':
                case ')':
                    break;

                case ' ':
                case '\t':
                case '/':
                    separator = true;
                    break;

                default:
                    if (separator && word.length() > 0)
                    {
                        words.add(word.toString());
                        word = new StringBuilder();
                    }
                    word.append(c);
                    separator = false;
                    break;
            }
        }
        if (word.length() > 0)
        {
            words.add(word.toString());
        }
        return words;
    }
}
