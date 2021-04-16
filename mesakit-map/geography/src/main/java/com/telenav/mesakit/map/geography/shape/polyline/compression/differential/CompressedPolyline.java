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

package com.telenav.mesakit.map.geography.shape.polyline.compression.differential;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.kivakit.core.collections.primitive.array.bits.BitArray;
import com.telenav.kivakit.core.collections.primitive.array.bits.io.BitReader;
import com.telenav.kivakit.core.collections.primitive.array.bits.io.BitWriter;
import com.telenav.kivakit.core.collections.primitive.list.ByteList;
import com.telenav.kivakit.core.kernel.interfaces.numeric.Sized;
import com.telenav.kivakit.core.kernel.language.collections.CompressibleCollection;
import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.kivakit.core.kernel.language.values.count.BitCount;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;

import java.util.Arrays;
import java.util.Collection;

/**
 * Stores a sequence of locations defining a shape on the map. Locations that are near to a previous location in the
 * line may be stored relative to that location to decrease memory use.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPolyline.class)
@UmlExcludeSuperTypes(CompressibleCollection.class)
public class CompressedPolyline extends Polyline implements CompressibleCollection
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private static final int PROXIMITY_TYPE_BITS = Count.count(Proximity.values()).decremented().bitsToRepresent().asInt();

    public static CompressedPolyline fromBitArray(final BitArray bits)
    {
        return new CompressedPolyline(bits);
    }

    public static CompressedPolyline fromLocationSequence(final Location... locations)
    {
        return new CompressedPolyline(Arrays.asList(locations));
    }

    public static CompressedPolyline fromLocationSequence(final Iterable<Location> locations)
    {
        return new CompressedPolyline(locations);
    }

    public static CompressedPolyline fromLocationSequence(final LocationSequence sequence)
    {
        return fromLocationSequence(sequence.locationSequence());
    }

    public static void main(final String[] args)
    {
        for (final var proximity : Proximity.values())
        {
            if (proximity != Proximity.DISTANCE8)
            {
                show(proximity);
            }
        }
    }

    /**
     * Variable length relative compression of location data is achieved using 12, 16 and 32 bit values to store
     * locations in dm7 precision (where dm7 denotes 7 significant digits, like 34.1234567).
     * <p>
     * Since one dm7 unit is 0.0110574 meters at the equator (see Angle.java for an explanation) this means:
     * <ul>
     * <li>12 bits is 2^12 and can store up to 4,096 dm7 units or 45.29 meters.</li>
     * <li>16 bits is 2^16 and can store up to 65,536 dm7 units or 724.66 meters.</li>
     * <li>32 bits stores 2^32 and can store up to 4,294,967,296 dm7 units or 47,491 kilometers</li>
     * </ul>
     * <p>
     * NOTE: Earth is 40,075 km in circumference.
     */
    private enum Proximity
    {
        DISTANCE1(new Encoding(9)), // 5.7 meters (18.6 feet)
        DISTANCE2(new Encoding(10)), // 11.4 meters (37.3 feet)
        DISTANCE3(new Encoding(11)), // 22.8 meters (74.7 feet)
        DISTANCE4(new Encoding(12)), // 45.5 meters (149.4 feet)
        DISTANCE5(new Encoding(13)), // 91.1 meters (298.9 feet)
        DISTANCE6(new Encoding(14)), // 182.2 meters (597.8 feet)
        DISTANCE7(new Encoding(16)), // 182.2 meters (597.8 feet)
        DISTANCE8(new Encoding(32)); // 20020.7 km (12440.3 miles)

        private final Encoding encoding;

        Proximity(final Encoding encoding)
        {
            this.encoding = encoding;
        }

        public int identifier()
        {
            return ordinal();
        }

        public long read(final BitReader reader, final long last)
        {
            final var precision = Precision.DM7;

            final var lastLatitude = Location.latitude(last);
            final var lastLongitude = Location.longitude(last);

            assert precision.isValidLatitude(lastLatitude);
            assert precision.isValidLongitude(lastLongitude);

            if (encoding != null)
            {
                final var bits = encoding.bits();

                final var latitudeOffset = Ints.signExtend(reader.read(bits), bits);
                final var longitudeOffset = Ints.signExtend(reader.read(bits), bits);

                final var latitude = lastLatitude + latitudeOffset;
                final var longitude = lastLongitude + longitudeOffset;

                assert precision.isValidLatitude(latitude);
                assert precision.isValidLongitude(longitude);

                return Location.toLong(latitude, longitude);
            }
            return -1;
        }

        public boolean write(final BitWriter writer, final int atLatitude,
                             final int atLongitude, final int lastLatitude, final int lastLongitude)
        {
            final var precision = Precision.DM7;

            // Ensure parameters
            assert precision.isValidLatitude(atLatitude);
            assert precision.isValidLongitude(atLongitude);
            assert precision.isValidLatitude(lastLatitude);
            assert precision.isValidLongitude(lastLongitude);

            // Compute latitude and longitude relative to last location
            final var latitudeOffset = atLatitude - lastLatitude;
            final var longitudeOffset = atLongitude - lastLongitude;

            assert precision.isValidLatitudeOffset(latitudeOffset);
            assert precision.isValidLongitudeOffset(longitudeOffset);

            // Get minimum and maximum latitude and longitude
            if (encoding != null)
            {
                final var minimumLatitudeOffset = (int) encoding.minimumLatitudeOffset();
                final var minimumLongitudeOffset = (int) encoding.minimumLongitudeOffset();
                final var maximumLatitudeOffset = (int) encoding.maximumLatitudeOffset();
                final var maximumLongitudeOffset = (int) encoding.maximumLongitudeOffset();

                assert precision.isValidLatitudeOffset(minimumLatitudeOffset);
                assert precision.isValidLongitudeOffset(minimumLongitudeOffset);
                assert precision.isValidLatitudeOffset(maximumLatitudeOffset);
                assert precision.isValidLongitudeOffset(maximumLongitudeOffset);

                // Get the number of bits we're using to encode the relative value
                final var bits = encoding.bits();

                // If the relative latitude can be expressed by this proximity
                if (Ints.isBetween(latitudeOffset, minimumLatitudeOffset, maximumLatitudeOffset)
                        && Ints.isBetween(longitudeOffset, minimumLongitudeOffset, maximumLongitudeOffset))
                {
                    // write out the proximity identifier
                    writer.write(identifier(), PROXIMITY_TYPE_BITS);

                    // followed by the relative latitude and longitude
                    writer.write(latitudeOffset, bits);
                    writer.write(longitudeOffset, bits);

                    return true;
                }
            }
            return false;
        }
    }

    private static class Encoding
    {
        private final int bits;

        private final long maximumLatitudeOffset;

        private final long maximumLongitudeOffset;

        private final long minimumLatitudeOffset;

        private final long minimumLongitudeOffset;

        public Encoding(final int bits)
        {
            this.bits = bits;
            maximumLatitudeOffset = Precision.DM7.inRangeLatitudeOffset(BitCount.bitCount(this.bits).maximumSigned());
            maximumLongitudeOffset = Precision.DM7.inRangeLongitudeOffset(BitCount.bitCount(this.bits).maximumSigned());
            minimumLatitudeOffset = Precision.DM7.inRangeLatitudeOffset(BitCount.bitCount(this.bits).minimumSigned());
            minimumLongitudeOffset = Precision.DM7.inRangeLongitudeOffset(BitCount.bitCount(this.bits).minimumSigned());
        }

        public int bits()
        {
            return bits;
        }

        public long maximumLatitudeOffset()
        {
            return maximumLatitudeOffset;
        }

        public long maximumLongitudeOffset()
        {
            return maximumLongitudeOffset;
        }

        public long minimumLatitudeOffset()
        {
            return minimumLatitudeOffset;
        }

        public long minimumLongitudeOffset()
        {
            return minimumLongitudeOffset;
        }
    }

    /** Bits defining each location in the polyline */
    private BitArray bits;

    /** Total length of this polyline */
    private long lengthInMillimeters;

    /** Locations in decimal */
    private long[] locationsInDecimal;

    protected CompressedPolyline()
    {
    }

    /**
     * @param bits Construct from bits
     */
    private CompressedPolyline(final BitArray bits)
    {
        assert bits != null;
        assert bits.isInitialized();
        this.bits = bits;
    }

    /**
     * Construct by compressing a sequence of locations
     */
    private CompressedPolyline(final Iterable<Location> locations)
    {
        assert locations != null;
        bits = compress(locations);
    }

    /**
     * @return This polyline as a byte array
     */
    public ByteList asBytes()
    {
        return bits().bytes();
    }

    public BitArray compress(final Iterable<Location> locations)
    {
        final var array = new BitArray("CompressedPolyline.compressed");
        array.initialSize(512);
        array.initialize();

        try (final var writer = array.writer())
        {
            if (locations instanceof Collection)
            {
                writeTo(writer, ((Collection<?>) locations).size(), locations);
            }
            else if (locations instanceof Sized)
            {
                writeTo(writer, ((Sized) locations).size(), locations);
            }
            else
            {
                writeTo(writer, -1, locations);
            }
        }
        if (DEBUG.isDebugOn())
        {
            DEBUG.trace("CompressedPolyline with $ locations is $ bits ($ bytes): $",
                    Count.count(locations), array.size(), array.bytes().size(), array.bytes());
        }
        return array;
    }

    @Override
    public Method compress(final Method method)
    {
        return bits().compress(method);
    }

    @Override
    public Method compressionMethod()
    {
        return Method.RESIZE;
    }

    /**
     * @return A decompressed heavyweight polyline for use in testing since {@link CompressedPolyline} is already a
     * Polyline and doesn't need to be decompressed.
     */
    public Polyline decompress()
    {
        // Construct polyline from long values
        return Polyline.fromLongs(decompressedInDecimal());
    }

    public long[] decompressedInDecimal()
    {
        // Start reading from the byte array,
        final var reader = bits().reader();

        // and get the number of locations either as 3 bit or 15 bit value
        final var size = reader.readFlexibleInt(3, 15);

        // allocate storage for the locations
        final var locations = new long[size];

        // and loop through each location
        var last = 0L;
        final var proximities = Proximity.values();
        for (var index = 0; index < size; index++)
        {
            // getting the proximity of the location (DISTANCE1 to DISTANCE8).
            final var proximity = proximities[reader.read(PROXIMITY_TYPE_BITS)];

            // Read the right number of bits to get the location, assign it to the location store
            // and remember it as the last location
            last = proximity.read(reader, last);

            final var at = last;

            locations[index] = at;
        }
        return locations;
    }

    @Override
    public Distance length()
    {
        if (lengthInMillimeters == 0)
        {
            computeLength();
        }
        return Distance.millimeters(lengthInMillimeters);
    }

    @Override
    public long[] locationsInDecimal()
    {
        if (locationsInDecimal == null)
        {
            locationsInDecimal = decompressedInDecimal();
        }
        return locationsInDecimal;
    }

    @Override
    public final int size()
    {
        return locationsInDecimal().length;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static void show(final Proximity proximity)
    {
        final var encoding = proximity.encoding;
        if (encoding != null)
        {
            final var bits = encoding.bits();
            final var minimum = (int) encoding.minimumLatitudeOffset();
            final var maximum = (int) encoding.maximumLatitudeOffset();
            System.out.println(bits + " bits " + proximity + " distance = " + Precision.DM7.toLatitudinalDistance(minimum, maximum));
        }
        System.out.println();
    }

    private BitArray bits()
    {
        return bits;
    }

    private void computeLength()
    {
        long previous = -1;
        var lengthInMillimeters = 0L;
        final var locations = decompressedInDecimal();
        for (final var at : locations)
        {
            if (previous >= 0)
            {
                final var fromLatitudeInDm7 = Location.latitude(previous);
                final var fromLongitudeInDm7 = Location.longitude(previous);
                final var toLatitudeInDm7 = Location.latitude(at);
                final var toLongitudeInDm7 = Location.longitude(at);
                final var millimeters = Location.equirectangularDistanceBetweenInMillimeters(
                        fromLatitudeInDm7, fromLongitudeInDm7,
                        toLatitudeInDm7, toLongitudeInDm7);
                lengthInMillimeters += millimeters;
            }
            previous = at;
        }
        this.lengthInMillimeters = lengthInMillimeters;
    }

    /**
     * Writes the given sequence of locations to the given bit writer in the given precision
     */
    private void writeTo(final BitWriter writer, int size, final Iterable<Location> locations)
    {
        var lastLatitude = 0;
        var lastLongitude = 0;
        if (size < 0)
        {
            size = Count.count(locations).asInt();
        }
        writer.writeFlexibleInt(3, 15, size);
        Location previous = null;
        var lengthInMillimeters = 0L;
        final var proximities = Proximity.values();
        for (final var location : locations)
        {
            final var atLatitude = location.latitudeInDm7();
            final var atLongitude = location.longitudeInDm7();
            var written = false;
            for (final var proximity : proximities)
            {
                if (proximity.write(writer, atLatitude, atLongitude, lastLatitude, lastLongitude))
                {
                    written = true;
                    break;
                }
            }
            assert written;
            if (previous != null)
            {
                lengthInMillimeters += previous.distanceTo(location).asMillimeters();
            }
            previous = location;
            lastLatitude = atLatitude;
            lastLongitude = atLongitude;
        }
        this.lengthInMillimeters = lengthInMillimeters;
    }
}
