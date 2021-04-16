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
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.kivakit.core.kernel.language.values.count.Range;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

public class Codec
{
    private final GeohashAlphabet alphabet;

    public Codec(final GeohashAlphabet alphabet)
    {
        this.alphabet = alphabet;
    }

    public Rectangle decode(final Code code)
    {
        if (code.isRoot())
        {
            return Rectangle.MAXIMUM;
        }
        else
        {
            // The following algorithm is taken from http://en.wikipedia.org/wiki/Geohash
            final var codeBits = codeToBitArray(code);
            final var longitudeBits = new BitArray.Builder();
            final var latitudeBits = new BitArray.Builder();
            var index = 0;
            for (final boolean bit : codeBits.allBits())
            {
                if (index % 2 == 0)
                {
                    longitudeBits.appendBit(bit);
                }
                else
                {
                    latitudeBits.appendBit(bit);
                }
                index++;
            }

            final var longitudeRange = bitsToCoordinateRange(longitudeBits.build(), Longitude.RANGE);
            final var latitudeRange = bitsToCoordinateRange(latitudeBits.build(), Latitude.RANGE);

            final var north = Latitude.angle(latitudeRange.maximum());
            final var south = Latitude.angle(latitudeRange.minimum());
            final var east = Longitude.angle(longitudeRange.maximum());
            final var west = Longitude.angle(longitudeRange.minimum());

            final var bottomLeft = new Location(south, west);
            final var topRight = new Location(north, east);

            return Rectangle.fromLocations(bottomLeft, topRight);
        }
    }

    public Code encode(final Location location, final int resolution)
    {
        ensure(resolution >= 0 && resolution <= alphabet.maximumTextLength(),
                "The resolution must be between 0 and $ (inclusive)", alphabet.maximumTextLength());

        // The following algorithm is taken from
        // http://architects.dzone.com/articles/designing-spacial-index
        final var bitCount = alphabet.bitsPerCharacter() * resolution;
        final var latitudeBitCount = bitCount / 2;
        final var longitudeBitCount = bitCount - latitudeBitCount;
        final var latitudeBits = coordinateToBits(location.latitude(), Latitude.RANGE, latitudeBitCount);
        final var longitudeBits = coordinateToBits(location.longitude(), Longitude.RANGE, longitudeBitCount);

        // Merge the two bit arrays by interleaving their bits
        final var geohashBits = new BitArray.Builder();
        for (var i = 0; i < longitudeBits.length(); i++)
        {
            geohashBits.appendBit(longitudeBits.isSet(i));
            if (i < latitudeBits.length())
            {
                geohashBits.appendBit(latitudeBits.isSet(i));
            }
        }
        return bitArrayToCode(geohashBits.build());
    }

    private Code bitArrayToCode(final BitArray bits)
    {
        // Group bits in sets of 'bitsPerCharacter' and read the sets as integers
        // Each integer is an index in the character alphabet
        final var codeBuilder = new StringBuilder();
        final var geohashBinary = bits.toString();
        final var resolution = bits.length() / alphabet.bitsPerCharacter();
        for (var i = 0; i < resolution; i++)
        {
            final var start = alphabet.bitsPerCharacter() * i;
            final var end = alphabet.bitsPerCharacter() * (i + 1);
            final var indexBinary = geohashBinary.substring(start, end);
            final var index = Integer.parseInt(indexBinary, 2);
            codeBuilder.append(alphabet.get(index));
        }
        return new Code(alphabet, codeBuilder.toString());
    }

    /**
     * @param coordinateBits a sequence of bits representing the position of the coordinate inside its coordinate range
     * @param coordinateRange [-90,90] for latitude, and [-180,180] for longitude
     * @return The smallest range of coordinates represented by the given bit sequence. A {@link Geohash} with a large
     * boundary will have a wide range of coordinates and a {@link Geohash} with a small boundary will have a narrow
     * range of coordinates.
     */
    private Range<Angle> bitsToCoordinateRange(final BitArray coordinateBits, final Range<Angle> coordinateRange)
    {
        var lower = coordinateRange.minimum();
        var upper = coordinateRange.maximum();
        for (var i = 0; i < coordinateBits.length(); i++)
        {
            final var middle = lower.bisect(upper, Chirality.CLOCKWISE);
            if (coordinateBits.isSet(i))
            {
                lower = middle;
            }
            else
            {
                upper = middle;
            }
        }
        return new Range<>(lower, upper);
    }

    private BitArray codeToBitArray(final Code code)
    {
        final var builder = new BitArray.Builder();
        for (var i = 0; i < code.length(); i++)
        {
            final var c = code.charAt(i);
            builder.appendBits(alphabet.indexOf(c), alphabet.bitsPerCharacter());
        }
        return builder.build();
    }

    /**
     * @param coordinate the coordinate (either latitude or longitude) as decimal degrees
     * @param coordinateRange [-90,90] for latitude, and [-180,180] for longitude
     * @param resolution the number of bits to be returned; the more bits are returned, the greater the precision
     * @return a bit array representing the position of the coordinate inside its coordinate range. The range is split
     * recursively in two equal ranges, and at each step a bit is added to the result. If the coordinate is found in the
     * lower half of the range the bit is 0, otherwise it's 1.
     */
    private BitArray coordinateToBits(final Angle coordinate, final Range<Angle> coordinateRange, final int resolution)
    {
        final var bits = new BitArray.Builder();
        var lower = coordinateRange.minimum();
        var upper = coordinateRange.maximum();
        for (var i = 0; i < resolution; i++)
        {
            final var middle = lower.bisect(upper, Chirality.CLOCKWISE);
            if (coordinate.isGreaterThanOrEqualTo(middle))
            {
                bits.appendBit(true);
                lower = middle;
            }
            else
            {
                bits.appendBit(false);
                upper = middle;
            }
        }
        return bits.build();
    }
}
