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

package com.telenav.aonia.map.ui.swing.map.graphics.canvas;

import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

public class Width
{
    public static Width meters(final double meters)
    {
        return of(Distance.meters(meters));
    }

    public static Width of(final Distance distance)
    {
        return new Width(distance);
    }

    public static Width pixels(final Float pixels)
    {
        return new Width(pixels);
    }

    private enum Metric
    {
        PIXELS,
        DISTANCE
    }

    @KivaKitIncludeProperty
    private final Metric metric;

    @KivaKitIncludeProperty
    private Float pixels;

    @KivaKitIncludeProperty
    private Distance distance;

    private Width(final Distance distance)
    {
        metric = Metric.DISTANCE;
        this.distance = ensureNotNull(distance);
    }

    private Width(final Float pixels)
    {
        metric = Metric.PIXELS;
        this.pixels = ensureNotNull(pixels);
    }

    public Distance asDistance(final MapCanvas canvas)
    {
        if (metric == Metric.PIXELS)
        {
            return canvas.distance(pixels);
        }
        return distance;
    }

    public float asPixels(final MapCanvas canvas)
    {
        if (metric == Metric.DISTANCE)
        {
            return canvas.awtDistance(distance);
        }
        return pixels;
    }

    public Width fattened(final Percent percent)
    {
        return times(percent.plus(Percent._100));
    }

    public Width minus(final MapCanvas canvas, final Width that)
    {
        return new Width(asDistance(canvas).minus(that.asDistance(canvas)));
    }

    public Width plus(final MapCanvas canvas, final Width that)
    {
        return new Width(asDistance(canvas).add(that.asDistance(canvas)));
    }

    public Width times(final Percent scaleFactor)
    {
        if (metric == Metric.DISTANCE)
        {
            return new Width(distance.times(scaleFactor));
        }
        else
        {
            return new Width((float) (pixels * scaleFactor.asUnitValue()));
        }
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }
}
