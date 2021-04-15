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

package com.telenav.aonia.map.road.name.parser.locales.english;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.ParsedRoadName;
import com.telenav.aonia.map.road.name.parser.RoadNameParser;
import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
@Ignore
public class EnglishUnitedStatesRoadNameParserTest extends UnitTest
{
    @Test
    public void testAmbiguities()
    {
        test("7th St Bike Lane", "7th St Bike Lane");
        test("West Green Lake Way NW", "west green lake way northwest");
        test("North Old W", "north old west");
        test("West Wing E", "west wing east");
    }

    @Test
    public void testInterstates()
    {
        test("I-5", "interstate 5");
        test("I-5", "i-5");
        test("I-90", "i 90");
    }

    @Test
    public void testNamedRoads()
    {
        test("Main Street", "main st");
        test("Marlon Avenue", "marlon ave");
        test("Main Street N", "main street north");
        test("Main Street N", "main street n");
        test("Western", "western");
        test("Western Circle", "western cir");
        test("NW Circle Lane", "northwest circle ln");
    }

    @Test
    public void testNumericOrdinals()
    {
        test("12th Drive", "12th Drive");
        test("NW 52nd Avenue", "nw fifty second ave");
        test("1st Avenue NW", "first ave nw");
        test("119th Avenue NW", "one hundred and nineteenth ave nw");
        test("500th Avenue SW", "five hundredth ave southwest");
        test("49th Way E", "forty ninth way east");
        test("1119th Avenue NW", "1119th ave nw");
        test("W 37th Street", "west 37th street");
    }

    @Test
    public void testStateRoutes()
    {
        test("WA-520", "WA-520");
        test("WA-520", "WA 520");
    }

    @Test
    public void testSymbols()
    {
        test("Can't Drive", "can't dr");
        test("Parlez-Vous Boulevard", "parlez-vous boulevard");
    }

    @Test
    public void testUsHighways()
    {
        test("US-9", "u.s. highway 9");
        test("US-9", "u.s. hwy 9");
        test("US-43", "u.s. route 43");
        test("US-99", "united states highway 99");
    }

    private String normalize(final String string)
    {
        return string.replaceAll("[ ,'.]", "");
    }

    private void test(final String expected, final String given)
    {
        final var locale = MapLocale.ENGLISH_UNITED_STATES.get();
        final var parser = RoadNameParser.get(locale);
        assert parser != null;
        final ParsedRoadName parsed = parser.parse(RoadName.forName(given));
        ensureEqual(expected, parsed.toString());
        ensureEqual(normalize(given), normalize(parsed.asRawRoadName().name()));
        trace(given + " -> " + parsed);
    }
}
