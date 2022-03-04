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

package com.telenav.mesakit.map.geography.shape.polyline.compression.huffman;

import com.telenav.kivakit.primitive.collections.CompressibleCollection;
import com.telenav.kivakit.primitive.collections.array.scalars.ByteArray;
import com.telenav.kivakit.primitive.collections.list.ByteList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * <b>Work in Progress</b>
 * <p>
 * Stores a sequence of locations defining a shape on the map. Locations that are near to a previous location in the
 * line may be stored relative to that location to decrease memory use.
 *
 * @author jonathanl (shibo)
 */
public class HuffmanPolyline extends Polyline implements CompressibleCollection
{
    private static final HuffmanPolylineCodec CODEC = new HuffmanPolylineCodec();

    /** Data defining each location in the polyline */
    private ByteList compressed;

    /** Total length of this polyline */
    private long lengthInMillimeters;

    /** Locations as long values */
    private long[] locations;

    /**
     * @param compressed Construct from compressed data
     */
    public HuffmanPolyline(ByteList compressed)
    {
        assert compressed != null;
        this.compressed = compressed;
        this.compressed.reset();
    }

    /**
     * Construct by compressing another polyline
     */
    public HuffmanPolyline(Polyline polyline)
    {
        var compressed = new ByteArray("polyline-data");
        compressed.initialSize(16);
        compressed.initialize();
        this.compressed = compressed;

        CODEC.encode(compressed, polyline);
    }

    protected HuffmanPolyline()
    {
    }

    /**
     * @return This polyline as a byte array
     */
    public ByteList asBytes()
    {
        return compressed;
    }

    @Override
    public Method compress(CompressibleCollection.Method method)
    {
        return asBytes().compress(method);
    }

    @Override
    public Method compressionMethod()
    {
        return Method.RESIZE;
    }

    /**
     * @return A decompressed heavyweight polyline for use in testing since {@link HuffmanPolyline} is already a
     * Polyline and doesn't need to be decompressed.
     */
    public Polyline decompress()
    {
        // Construct polyline from long values
        return new Polyline(decompressedLongs());
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
        if (locations == null)
        {
            locations = decompressedLongs();
        }
        return locations;
    }

    @Override
    public final int size()
    {
        return locationsInDecimal().length;
    }

    private void computeLength()
    {
        long previous = -1;
        var lengthInMillimeters = 0L;
        var locations = decompressedLongs();
        for (var at : locations)
        {
            if (previous >= 0)
            {
                var fromLatitude = Location.latitude(previous);
                var fromLongitude = Location.longitude(previous);
                var toLatitude = Location.latitude(at);
                var toLongitude = Location.longitude(at);
                var millimeters = Location.equirectangularDistanceBetweenInMillimeters(
                        fromLatitude, fromLongitude,
                        toLatitude, toLongitude);

                lengthInMillimeters += millimeters;
            }
            previous = at;
        }
        this.lengthInMillimeters = lengthInMillimeters;
    }

    private long[] decompressedLongs()
    {
        return CODEC.decode(asBytes());
    }
}
