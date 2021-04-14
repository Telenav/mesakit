////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.locales.english;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.region.project.MapRegionUnitTest;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.ParsedRoadName;
import com.telenav.aonia.map.road.name.parser.RoadNameParser;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
@Ignore
public class EnglishCanadaRoadNameParserTest extends MapRegionUnitTest
{
    @Test
    public void testAmbiguities()
    {
        test("Ln W Royal York N Simpson", "Lane West Royal York North Simpson");
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
    public void testSymbols()
    {
        test("Can't Drive", "can't dr");
        test("Parlez-Vous Boulevard", "parlez-vous boulevard");
    }

    private String normalize(final String string)
    {
        return string.replaceAll("[ ,'.]", "");
    }

    private void test(final String expected, final String given)
    {
        final ParsedRoadName parsed = RoadNameParser.get(MapLocale.ENGLISH_CANADA.get()).parse(RoadName.forName(given));
        ensureEqual(expected, parsed.toString());
        ensureEqual(normalize(given), normalize(parsed.asRawRoadName().name()));
        trace(given + " -> " + parsed);
    }
}
