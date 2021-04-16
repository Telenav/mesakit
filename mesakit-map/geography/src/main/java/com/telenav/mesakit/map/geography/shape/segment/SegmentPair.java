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

package com.telenav.mesakit.map.geography.shape.segment;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramSegment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.mesakit.map.measurements.geographic.Heading;

/**
 * A pair of segments.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramSegment.class)
public class SegmentPair
{
    /**
     * The first segment in this pair
     */
    @UmlRelation(label = "first segment")
    private final Segment first;

    /**
     * The second segment in this pair
     */
    @UmlRelation(label = "second segment")
    private final Segment second;

    public SegmentPair(final Segment first, final Segment second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * @return The acute angle (smallest) difference between the first segment's heading and the second segment's
     * heading
     */
    public Angle angle()
    {
        return first.heading().difference(second.heading(), Chirality.SMALLEST);
    }

    /**
     * @return This segment pair joined at any common point and facing outwards like clock hands, or null if the
     * segments don't share a common point
     */
    public SegmentPair asClockHands()
    {
        if (first.start().equals(second.start()))
        {
            return this;
        }
        if (first.end().equals(second.start()))
        {
            return new SegmentPair(first.reversed(), second);
        }
        if (first.start().equals(second.end()))
        {
            return new SegmentPair(first, second.reversed());
        }
        if (first.end().equals(second.end()))
        {
            return new SegmentPair(first.reversed(), second.reversed());
        }
        return null;
    }

    public Heading bisect(final Chirality chirality)
    {
        return first.heading().bisect(second.heading(), chirality);
    }

    /**
     * @return The first segment in this pair
     */
    public Segment first()
    {
        return first;
    }

    /**
     * @return The second segment in this pair
     */
    public Segment second()
    {
        return second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[SegmentPair angle = " + angle() + "]";
    }
}
