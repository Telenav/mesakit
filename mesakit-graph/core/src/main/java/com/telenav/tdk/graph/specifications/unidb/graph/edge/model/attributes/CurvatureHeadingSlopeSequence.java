////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes;

import com.google.common.primitives.*;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.map.measurements.*;

public class CurvatureHeadingSlopeSequence
{
    public static int NULL_SLOPE = 91;

    private long[] curvatures;

    private long[] headings;

    private byte[] slopes;

    public Count count()
    {
        return Count.of(size());
    }

    public AdasCurvature curvature(final int index)
    {
        final var value = (int) curvatures[index];
        return value == AdasCurvature.NULL ? null : new AdasCurvature(value);
    }

    public ObjectList<AdasCurvature> curvatures()
    {
        final var curvatures = new ObjectList<AdasCurvature>(Maximum.of(count()));
        for (var index = 0; index < size(); index++)
        {
            curvatures.add(curvature(index));
        }
        return curvatures;
    }

    public CurvatureHeadingSlopeSequence curvatures(final long[] curvatures)
    {
        this.curvatures = curvatures;
        return this;
    }

    public CurvatureHeadingSlopeSequence curvatures(final ObjectList<AdasCurvature> curvatures)
    {
        this.curvatures = curvatures.quantized();
        return this;
    }

    public CurvatureHeadingSlope get(final int index)
    {
        return new CurvatureHeadingSlope(curvature(index), heading(index), slope(index));
    }

    public Heading heading(final int index)
    {
        final var value = headings[index];
        return value == Heading.NULL ? null : Heading.degrees(value);
    }

    public ObjectList<Heading> headings()
    {
        final var headings = new ObjectList<Heading>(Maximum.of(count()));
        for (var index = 0; index < size(); index++)
        {
            headings.add(heading(index));
        }
        return headings;
    }

    public CurvatureHeadingSlopeSequence headings(final long[] headings)
    {
        this.headings = headings;
        return this;
    }

    public CurvatureHeadingSlopeSequence headings(final ObjectList<Heading> headings)
    {
        this.headings = headings.quantized();
        return this;
    }

    public boolean isEmpty()
    {
        return curvatures.length == 0;
    }

    public CurvatureHeadingSlopeSequence maybeReverse(final boolean reversed)
    {
        return reversed ? reverse() : this;
    }

    public CurvatureHeadingSlopeSequence reverse()
    {
        Longs.reverse(curvatures);
        Longs.reverse(headings);
        Bytes.reverse(slopes);
        return this;
    }

    public int size()
    {
        return curvatures.length;
    }

    public Slope slope(final int index)
    {
        final var value = slopes[index];
        return value == NULL_SLOPE ? null : Slope.degrees(value);
    }

    public ObjectList<Slope> slopes()
    {
        final var slopes = new ObjectList<Slope>(Maximum.of(count()));
        for (var index = 0; index < size(); index++)
        {
            slopes.add(slope(index));
        }
        return slopes;
    }

    public CurvatureHeadingSlopeSequence slopes(final byte[] slopes)
    {
        this.slopes = slopes;
        return this;
    }

    public CurvatureHeadingSlopeSequence slopes(final ObjectList<Slope> slopes)
    {
        final var quantized = slopes.quantized();
        this.slopes = new byte[quantized.length];
        for (int i = 0; i < quantized.length; i++)
        {
            this.slopes[i] = (byte) quantized[i];
        }
        return this;
    }
}
