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

package com.telenav.mesakit.map.utilities.geohash;

import com.telenav.kivakit.testing.UnitTest;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import org.junit.Test;

import java.util.Collection;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * Note that the coordinates for geohashing are restricted from -85 to 85 latitude
 *
 * @author Mihai Chintoanu
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SpellCheckingInspection")
public class GeohashTest extends UnitTest
{
    private final GeohashAlphabet nonDefaultAlphabet = new GeohashAlphabet()
    {
        @Override
        public int bitsPerCharacter()
        {
            return unsupported();
        }

        @Override
        public Collection<Character> characters()
        {
            return unsupported();
        }

        @Override
        public char get(int index)
        {
            return unsupported();
        }

        @Override
        public int indexOf(char character)
        {
            return unsupported();
        }

        @Override
        public boolean isValid(String text)
        {
            return true;
        }

        @Override
        public int maximumTextLength()
        {
            return unsupported();
        }

        @Override
        public int numberOfCharacters()
        {
            return unsupported();
        }
    };

    @Test
    public void testBounds()
    {
        expectBoundsForCode(-85, -180, 85, 180, "");
        expectBoundsForCode(42.5, -180, 85, -135, "b");
        expectBoundsForCode(65.5761718, -151.171875, 65.7421875, -150.8203125, "bske");
        expectBoundsForCode(-26.5625, -29.53125, -25.234375, -28.125, "775");
    }

    @Test
    public void testChildren()
    {
        var geohash = new Geohash(new Code("b"));
        ensure(geohash.children().contains(new Geohash(new Code("be"))));
        ensureFalse(geohash.children().contains(new Geohash(new Code("bske"))));
    }

    @Test
    public void testDepth()
    {
        ensureEqual(0, Geohash.world().depth());
        ensureEqual(1, new Geohash(new Code("b")).depth());
        ensureEqual(4, new Geohash(new Code("best")).depth());
        var location = Location.degrees(0, 0);
        ensureEqual(1, new Geohash(location, 1).depth());
        ensureEqual(4, new Geohash(location, 4).depth());
    }

    @Test
    public void testEquals()
    {
        var code = "b";
        var location = Location.degrees(65.6, -151);
        var forCode = new Geohash(new Code(code));
        var forNonDefaultCode = new Geohash(new Code(nonDefaultAlphabet, code));

        ensureEqual(forCode, new Geohash(new Code(code)));
        ensureFalse(forCode.equals(new Geohash(new Code("y"))));
        ensureFalse(forCode.equals(forNonDefaultCode));
        ensureEqual(forCode, new Geohash(location, 1));
        ensureFalse(forCode.equals(new Geohash(location, 4)));
    }

    @Test
    public void testGeohash_Code()
    {
        var code = new Code("b");
        var geohash = new Geohash(code);
        ensureEqual(code, geohash.code());
    }

    @Test
    public void testGeohash_Location_Resolution()
    {
        expectCodeForLocationAndResolution("b", 65.6, -151, 1);
        expectCodeForLocationAndResolution("bske", 65.6, -151, 4);
        expectCodeForLocationAndResolution("775", -25.5, -29, 3);
    }

    @Test(expected = AssertionError.class)
    public void testGeohash_Location_negativeResolution()
    {
        new Geohash(Location.ORIGIN, -1);
    }

    @Test
    public void testGeohash_emptyCode()
    {
        ensureEqual(Geohash.world(), new Geohash(Code.root()));
        ensureEqual(Geohash.world(), new Geohash(new Code("")));
    }

    @Test(expected = AssertionError.class)
    public void testGeohash_invalidCode()
    {
        new Geohash(new Code("a"));
    }

    @Test(expected = AssertionError.class)
    public void testGeohash_nullCode()
    {
        new Geohash(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGeohash_nullGeohashAlphabet_Location_Resolution()
    {
        new Geohash(null, Location.ORIGIN, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testGeohash_nullLocation_Resolution()
    {
        new Geohash(null, 1);
    }

    @Test
    public void testIsWorld()
    {
        ensure(Geohash.world().isWorld());
        ensure(Geohash.world(nonDefaultAlphabet).isWorld());
        ensure(new Geohash(Code.root()).isWorld());
        ensure(new Geohash(Code.root(nonDefaultAlphabet)).isWorld());
        ensureFalse(new Geohash(new Code("b")).isWorld());
        ensureFalse(new Geohash(new Code(nonDefaultAlphabet, "b")).isWorld());
    }

    @Test
    public void testParent()
    {
        ensureNull(Geohash.world().parent());
        var geohash = new Geohash(new Code("b"));
        ensureEqual(Geohash.world(), geohash.parent());
    }

    @Test
    public void testWorld()
    {
        checkWorldCodeAndBounds(Geohash.world());
    }

    @Test
    public void testWorld_GeohashAlphabet()
    {
        checkWorldCodeAndBounds(Geohash.world(nonDefaultAlphabet));
    }

    @Test(expected = NullPointerException.class)
    public void testWorld_nullGeohashAlphabet()
    {
        checkWorldCodeAndBounds(Geohash.world(null));
    }

    private void checkWorldCodeAndBounds(Geohash world)
    {
        ensure(world.code().isRoot());
        var bounds = world.bounds();
        ensureEqual(Longitude.MINIMUM, bounds.left());
        ensureEqual(Longitude.MAXIMUM, bounds.right());
        ensureEqual(Latitude.MINIMUM, bounds.bottom());
        ensureEqual(Latitude.MAXIMUM, bounds.top());
    }

    private void expectBoundsForCode(double south,
                                     double west,
                                     double north,
                                     double east,
                                     String code)
    {
        var geohash = new Geohash(new Code(code));
        ensureEqual(Latitude.degrees(north), geohash.bounds().top());
        ensureEqual(Latitude.degrees(south), geohash.bounds().bottom());
        ensureEqual(Longitude.degrees(east), geohash.bounds().right());
        ensureEqual(Longitude.degrees(west), geohash.bounds().left());
    }

    private void expectCodeForLocationAndResolution(String code,
                                                    double latitude,
                                                    double longitude,
                                                    int resolution)
    {
        var location = Location.degrees(latitude, longitude);
        var geohash = new Geohash(location, resolution);
        ensureEqual(code, geohash.code().toString());
    }
}
