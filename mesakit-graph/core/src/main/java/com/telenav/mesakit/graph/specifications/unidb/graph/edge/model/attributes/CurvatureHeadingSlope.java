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

package com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes;

import com.telenav.kivakit.kernel.language.object.Hash;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.AdasCurvature.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * ADAS "CHS" strings contain curvature, heading and slope information. For example:
 *
 * <pre>
 * {431;200;-7|431;204;-25|430;207;-33|431;210;-34|431;212;-33|432;213;-32|431;217;-31}
 * </pre>
 *
 * @author jonathanl (shibo)
 */
public class CurvatureHeadingSlope
{
    public static CurvatureHeadingSlope UNDEFINED = new CurvatureHeadingSlope(AdasCurvature.UNDEFINED, Heading.NORTH, Slope.degrees(0));

    public static List<CurvatureHeadingSlope> of(final Edge edge)
    {
        final var chs = edge.tagValue("adas:chs");
        if (chs != null)
        {
            return of(chs.trim());
        }
        return new ArrayList<>();
    }

    public static CurvatureHeadingSlope of(final EdgeRelation relation, final Direction sign)
    {
        final var curvature = relation.tagValue("curvature");
        final var heading = relation.tagValue("heading");
        final var slope = relation.tagValue("slope_t");
        if (curvature != null && heading != null && slope != null)
        {
            return new CurvatureHeadingSlope(of(Double.parseDouble(curvature), sign),
                    heading(Double.parseDouble(heading)),
                    slope(Double.parseDouble(slope)));
        }
        return null;
    }

    public static List<CurvatureHeadingSlope> of(final String chs)
    {
        final List<CurvatureHeadingSlope> list = new ArrayList<>();
        for (final var triplet : chs.split("\\|"))
        {
            final var values = triplet.split(";");
            if (values.length == 3)
            {
                list.add(new CurvatureHeadingSlope(of(Double.parseDouble(values[0]), Direction.RIGHT),
                        heading(Double.parseDouble(values[1])),
                        slope(Double.parseDouble(values[2]))));
            }
        }
        return list;
    }

    private final AdasCurvature curvature;

    private final Heading heading;

    private final Slope slope;

    public CurvatureHeadingSlope(final AdasCurvature curvature, final Heading heading, final Slope slope)
    {
        this.curvature = curvature;
        this.heading = heading;
        this.slope = slope;
    }

    public AdasCurvature curvature()
    {
        return curvature;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof CurvatureHeadingSlope)
        {
            final var that = (CurvatureHeadingSlope) object;
            return curvature.equals(that.curvature) && heading.equals(that.heading)
                    && slope.equals(that.slope);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(curvature, heading, slope);
    }

    public Heading heading()
    {
        return heading;
    }

    public CurvatureHeadingSlope reversed()
    {
        return new CurvatureHeadingSlope(curvature, heading.reverse(), slope.reverse());
    }

    public Angle slope()
    {
        return slope;
    }

    private static Heading heading(final double value)
    {
        return Heading.degrees(value / 1_000.0);
    }

    private static AdasCurvature of(final double radiusOfCurvature, final Direction sign)
    {
        return new AdasCurvature(Distance.meters(1.0 / (radiusOfCurvature / 1_000_000.0)), sign);
    }

    private static Slope slope(final double value)
    {
        return Slope.degrees((value + 50.0) / 100.0);
    }
}
