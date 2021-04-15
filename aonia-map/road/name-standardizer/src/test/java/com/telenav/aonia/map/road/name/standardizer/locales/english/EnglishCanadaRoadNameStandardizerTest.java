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

package com.telenav.aonia.map.road.name.standardizer.locales.english;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.name.standardizer.RoadNameStandardizerTest;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class EnglishCanadaRoadNameStandardizerTest extends RoadNameStandardizerTest
{
    public EnglishCanadaRoadNameStandardizerTest()
    {
        super("Canada", "ENG");
    }

    @Test
    public void testAmbiguities()
    {
        test("47-49 St Clair Ave W", "47-49 Saint Clair Avenue West");
        test("3409 St Clair Ave E", "3409 St. Clair Avenue East");
        test("I-69 Business", "I 69 Business");
        test("13th St", "Thirteenth Street");
        test("Middlegate Rd E", "Middlegate Road (East)");
        test("E North Water St", "East North Water Street");
        test("U S S Antietam St", "U S S Antietam Street");
        test("Thousand Oaks Dr", "Thousand Oaks Drive");
        test("10 Mile Rd", "10 Mile Road");
        test("W Green Lake Way NW", "west green lake way northwest");
        test("N Old W", "north old west");
        test("W Wing E", "west wing east");
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
        test("West Dr", "West Drive");
        test("Main St N", "main street north");
        test("Main St N", "main street n");
        test("Marlon Ave", "marlon ave");
        test("Western", "western");
        test("Western Cir", "western circle");
        test("NW Circle Ln", "northwest circle lane");
    }

    @Test
    public void testNumericOrdinals()
    {
        test("NW 52nd Ave", "nw fifty second ave");
        test("1st Ave NW", "first ave nw");
        test("W 37th St", "west 37th street");
        test("119th Ave NW", "one hundred and nineteenth ave nw");
        test("500th Ave SW", "five hundredth ave southwest");
        test("49th Way E", "forty ninth way east");
        test("1119th Ave NW", "1119th ave nw");
    }

    @Test
    public void testSymbols()
    {
        test("Can't Dr", "can't drive");
        test("Parlez-Vous Blvd", "parlez-vous boulevard");
    }

    @Override
    protected MapLocale locale()
    {
        return MapLocale.ENGLISH_CANADA.get();
    }

    @Override
    protected String normalize(final String string)
    {
        return string.replaceAll("[.?]", "");
    }
}
