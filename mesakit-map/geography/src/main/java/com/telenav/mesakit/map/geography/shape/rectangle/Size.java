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

package com.telenav.mesakit.map.geography.shape.rectangle;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramRectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;

@UmlClassDiagram(diagram = DiagramRectangle.class)
public class Size implements Dimensioned
{
    public static final Size MAXIMUM = new Size(Width.MAXIMUM, Height.MAXIMUM);

    public static final Size ZERO = new Size(Width.ZERO, Height.ZERO);

    public static Size of(final Width width, final Height height)
    {
        return new Size(width, height);
    }

    public static Size of(final Distance width, final Distance height)
    {
        return new Size(Width.degrees(width.asDegrees()), Height.degrees(height.asDegrees()));
    }

    @UmlAggregation
    private final Width width;

    @UmlAggregation
    private final Height height;

    public Size(final Width width, final Height height)
    {
        if (width.asNanodegrees() < 0 || height.asNanodegrees() < 0)
        {
            throw new IllegalArgumentException("Size must have positive dimensions:  " + height + ", " + width);
        }
        this.width = width;
        this.height = height;
    }

    public Location asLocation()
    {
        return Location.degrees(height.asDegrees(), width.asDegrees());
    }

    public Rectangle asRectangle()
    {
        return Location.ORIGIN.rectangle(this);
    }

    @Override
    public Height height()
    {
        return height;
    }

    /**
     * @return True if both width and height of this size are greater than the given size
     */
    public boolean isGreaterThan(final Dimensioned that)
    {
        return width.isGreaterThan(that.width()) && height.isGreaterThan(that.height());
    }

    public Size scaledBy(final double widthMultiplier, final double heightMultiplier)
    {
        final var width = Math.min(360, this.width.asDegrees() * widthMultiplier);
        final var height = Math.min(180, this.height.asDegrees() * heightMultiplier);
        return new Size(Width.degrees(width), Height.degrees(height));
    }

    @Override
    public Size size()
    {
        return new Size(width(), height());
    }

    @Override
    public String toString()
    {
        return "[Size width = " + width + ", " + height + "]";
    }

    @Override
    public Width width()
    {
        return width;
    }

    public Size withHeight(final Height height)
    {
        return new Size(width, height);
    }

    public Size withWidth(final Width width)
    {
        return new Size(width, height);
    }
}
