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

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Test;

import java.util.Collection;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author Mihai Chintoanu
 * @author jonathanl (shibo)
 */
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
        public char get(final int index)
        {
            return unsupported();
        }

        @Override
        public int indexOf(final char character)
        {
            return unsupported();
        }

        @Override
        public boolean isValid(final String text)
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
        expectBoundsForCode(-90, -180, 90, 180, "");
        expectBoundsForCode(45, -180, 90, -135, "b");
        expectBoundsForCode(65.5664062, -151.171875, 65.7421875, -150.8203125, "best");
        expectBoundsForCode(-26.71875, -29.53125, -25.3125, -28.125, "777");
    }

    @Test
    public void testChildren()
    {
        final var geohash = new Geohash(new Code("b"));
        ensure(geohash.children().contains(new Geohash(new Code("be"))));
        ensureFalse(geohash.children().contains(new Geohash(new Code("best"))));
    }

    @Test
    public void testDepth()
    {
        ensureEqual(0, Geohash.world().depth());
        ensureEqual(1, new Geohash(new Code("b")).depth());
        ensureEqual(4, new Geohash(new Code("best")).depth());
        final var location = Location.degrees(0, 0);
        ensureEqual(1, new Geohash(location, 1).depth());
        ensureEqual(4, new Geohash(location, 4).depth());
    }

    @Test
    public void testEquals()
    {
        final var code = "b";
        final var location = Location.degrees(65.6, -151);
        final var forCode = new Geohash(new Code(code));
        final var forNonDefaultCode = new Geohash(new Code(nonDefaultAlphabet, code));

        ensureEqual(forCode, new Geohash(new Code(code)));
        ensureFalse(forCode.equals(new Geohash(new Code("y"))));
        ensureFalse(forCode.equals(forNonDefaultCode));
        ensureEqual(forCode, new Geohash(location, 1));
        ensureFalse(forCode.equals(new Geohash(location, 4)));
    }

    @Test
    public void testGeohash_Code()
    {
        final var code = new Code("b");
        final var geohash = new Geohash(code);
        ensureEqual(code, geohash.code());
    }

    @Test
    public void testGeohash_Location_Resolution()
    {
        expectCodeForLocationAndResolution("b", 65.6, -151, 1);
        expectCodeForLocationAndResolution("best", 65.6, -151, 4);
        expectCodeForLocationAndResolution("777", -25.5, -29, 3);
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
        final var geohash = new Geohash(new Code("b"));
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

    private void checkWorldCodeAndBounds(final Geohash world)
    {
        ensure(world.code().isRoot());
        final var bounds = world.bounds();
        ensureEqual(Longitude.MINIMUM, bounds.left());
        ensureEqual(Longitude.MAXIMUM, bounds.right());
        ensureEqual(Latitude.MINIMUM, bounds.bottom());
        ensureEqual(Latitude.MAXIMUM, bounds.top());
    }

    private void expectBoundsForCode(final double south, final double west, final double north, final double east,
                                     final String code)
    {
        final var geohash = new Geohash(new Code(code));
        ensureEqual(Latitude.degrees(north), geohash.bounds().top());
        ensureEqual(Latitude.degrees(south), geohash.bounds().bottom());
        ensureEqual(Longitude.degrees(east), geohash.bounds().right());
        ensureEqual(Longitude.degrees(west), geohash.bounds().left());
    }

    private void expectCodeForLocationAndResolution(final String code, final double latitude, final double longitude,
                                                    final int resolution)
    {
        final var location = Location.degrees(latitude, longitude);
        final var geohash = new Geohash(location, resolution);
        ensureEqual(code, geohash.code().toString());
    }
}
