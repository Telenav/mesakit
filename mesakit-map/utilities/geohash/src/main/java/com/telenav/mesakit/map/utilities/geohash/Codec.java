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

import com.telenav.kivakit.core.value.count.Range;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

@SuppressWarnings("SpellCheckingInspection")
public class Codec
{
    private final GeohashAlphabet alphabet;

    public Codec(GeohashAlphabet alphabet)
    {
        this.alphabet = alphabet;
    }

    public Rectangle decode(Code code)
    {
        if (code.isRoot())
        {
            return Rectangle.MAXIMUM;
        }
        else
        {
            // The following algorithm is taken from http://en.wikipedia.org/wiki/Geohash
            var codeBits = codeToBitArray(code);
            var longitudeBits = new BitArray.Builder();
            var latitudeBits = new BitArray.Builder();
            var index = 0;
            for (boolean bit : codeBits.allBits())
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

            var longitudeRange = bitsToCoordinateRange(longitudeBits.build(), Longitude.RANGE);
            var latitudeRange = bitsToCoordinateRange(latitudeBits.build(), Latitude.RANGE);

            var north = Latitude.angle(latitudeRange.inclusiveMaximum());
            var south = Latitude.angle(latitudeRange.minimum());
            var east = Longitude.angle(longitudeRange.inclusiveMaximum());
            var west = Longitude.angle(longitudeRange.minimum());

            var bottomLeft = new Location(south, west);
            var topRight = new Location(north, east);

            return Rectangle.fromLocations(bottomLeft, topRight);
        }
    }

    public Code encode(Location location, int resolution)
    {
        ensure(resolution >= 0 && resolution <= alphabet.maximumTextLength(),
                "The resolution must be between 0 and $ (inclusive)", alphabet.maximumTextLength());

        // The following algorithm is taken from
        // http://architects.dzone.com/articles/designing-spacial-index
        var bitCount = alphabet.bitsPerCharacter() * resolution;
        var latitudeBitCount = bitCount / 2;
        var longitudeBitCount = bitCount - latitudeBitCount;
        var latitudeBits = coordinateToBits(location.latitude(), Latitude.RANGE, latitudeBitCount);
        var longitudeBits = coordinateToBits(location.longitude(), Longitude.RANGE, longitudeBitCount);

        // Merge the two bit arrays by interleaving their bits
        var geohashBits = new BitArray.Builder();
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

    private Code bitArrayToCode(BitArray bits)
    {
        // Group bits in sets of 'bitsPerCharacter' and read the sets as integers
        // Each integer is an index in the character alphabet
        var codeBuilder = new StringBuilder();
        var geohashBinary = bits.toString();
        var resolution = bits.length() / alphabet.bitsPerCharacter();
        for (var i = 0; i < resolution; i++)
        {
            var start = alphabet.bitsPerCharacter() * i;
            var end = alphabet.bitsPerCharacter() * (i + 1);
            var indexBinary = geohashBinary.substring(start, end);
            var index = Integer.parseInt(indexBinary, 2);
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
    private Range<Angle> bitsToCoordinateRange(BitArray coordinateBits, Range<Angle> coordinateRange)
    {
        var lower = coordinateRange.minimum();
        var upper = coordinateRange.inclusiveMaximum();
        for (var i = 0; i < coordinateBits.length(); i++)
        {
            var middle = lower.bisect(upper, Chirality.CLOCKWISE);
            if (coordinateBits.isSet(i))
            {
                lower = middle;
            }
            else
            {
                upper = middle;
            }
        }
        return Range.rangeInclusive(lower, upper, Angle::degrees);
    }

    private BitArray codeToBitArray(Code code)
    {
        var builder = new BitArray.Builder();
        for (var i = 0; i < code.length(); i++)
        {
            var c = code.charAt(i);
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
    private BitArray coordinateToBits(Angle coordinate, Range<Angle> coordinateRange, int resolution)
    {
        var bits = new BitArray.Builder();
        var lower = coordinateRange.minimum();
        var upper = coordinateRange.inclusiveMaximum();
        for (var i = 0; i < resolution; i++)
        {
            var middle = lower.bisect(upper, Chirality.CLOCKWISE);
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
