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

import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.measurements.project.lexakai.DiagramMapMeasurementGeographic;

import static com.telenav.kivakit.ensure.Ensure.fail;

/**
 * A square area on a <a href="https://en.wikipedia.org/wiki/Cartesian_coordinate_system">Cartesian</a> plane, measured
 * as distance * distance. {@link Area}s can be created with the factory methods in this class or by calling {@link
 * Distance#squared()} or {@link Distance#by(Distance)}. Areas can be converted to different units, mathematically
 * combined and compared with each other.
 *
 * @author matthieun
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public class Area implements Comparable<Area>
{
    private static final long SQUARE_METERS_PER_SQUARE_KILOMETER = 1000 * 1000;

    private static final long SQUARE_FEET_PER_SQUARE_MILE = 5280 * 5280;

    private static final double SQUARE_METERS_PER_SQUARE_FOOT = 0.3048 * 0.3048;

    public static final Area MINIMUM = squareMeters(0L);

    public static final Area MAXIMUM = squareMeters(Integer.MAX_VALUE);

    public static final Area ONE_SQUARE_MILE = squareMiles(1);

    public static Area of(Distance width, Distance height)
    {
        return squareMeters(width.asMeters() * height.asMeters());
    }

    public static Area squareFeet(double squareFeet)
    {
        return squareMeters((long) (squareFeet * SQUARE_METERS_PER_SQUARE_FOOT));
    }

    public static Area squareKilometers(double squareKilometers)
    {
        return squareMeters(squareKilometers * SQUARE_METERS_PER_SQUARE_KILOMETER);
    }

    public static Area squareMeters(double squareMeters)
    {
        return new Area(squareMeters);
    }

    public static Area squareMiles(double squareMiles)
    {
        return squareFeet(squareMiles * SQUARE_FEET_PER_SQUARE_MILE);
    }

    private final double squareMeters;

    private Area(double squareMeters)
    {
        this.squareMeters = squareMeters;
    }

    public double asSquareFeet()
    {
        return asSquareMeters() / SQUARE_METERS_PER_SQUARE_FOOT;
    }

    public double asSquareKilometers()
    {
        return asSquareMeters() / SQUARE_METERS_PER_SQUARE_KILOMETER;
    }

    public double asSquareMeters()
    {
        return squareMeters;
    }

    public double asSquareMiles()
    {
        return asSquareFeet() / SQUARE_FEET_PER_SQUARE_MILE;
    }

    @Override
    public int compareTo(Area that)
    {
        var difference = squareMeters - that.asSquareMeters();
        if (difference == 0L)
        {
            return 0;
        }
        else
        {
            return difference > 0 ? 1 : -1;
        }
    }

    /**
     * @param that The other area to compare with.
     * @return The absolute value difference between the two areas.
     */
    public Area difference(Area that)
    {
        return squareMeters(Math.abs(asSquareMeters() - that.asSquareMeters()));
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Area)
        {
            var that = (Area) object;
            return asSquareMeters() == that.asSquareMeters();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.hashCode(asSquareMeters());
    }

    public boolean isGreaterThan(Area that)
    {
        return compareTo(that) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Area that)
    {
        return compareTo(that) >= 0;
    }

    public boolean isLessThan(Area that)
    {
        return compareTo(that) < 0;
    }

    public boolean isLessThanOrEqualTo(Area that)
    {
        return compareTo(that) <= 0;
    }

    public Area maximum(Area that)
    {
        return isGreaterThan(that) ? this : that;
    }

    public Area minimum(Area that)
    {
        return isLessThan(that) ? this : that;
    }

    /**
     * @param that The area to subtract from this one.
     * @return The newly calculated area. Note that if the passed in value is greater than this value 0 is returned.
     * There are no negative areas.
     */
    public Area minus(Area that)
    {
        var difference = asSquareMeters() - that.asSquareMeters();
        return squareMeters(difference < 0 ? 0 : difference);
    }

    public Area plus(Area that)
    {
        return squareMeters(asSquareMeters() + that.asSquareMeters());
    }

    public double ratio(Area divisor)
    {
        if (divisor.asSquareMeters() <= 0)
        {
            fail("Unable to divide by zero or a negative value");
            return -1;
        }
        return asSquareMeters() / divisor.asSquareMeters();
    }

    public Area times(double multiplier)
    {
        if (multiplier < 0)
        {
            return fail("Unable to scale by a negative value");
        }
        return squareMeters((long) (asSquareMeters() * multiplier));
    }

    @Override
    public String toString()
    {
        return asSquareMeters() + " square meters";
    }
}
