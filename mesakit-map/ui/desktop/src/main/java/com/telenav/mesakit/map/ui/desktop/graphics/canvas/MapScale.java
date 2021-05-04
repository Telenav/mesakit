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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas;

import com.telenav.kivakit.core.kernel.language.primitives.Doubles;

public enum MapScale
{
    STATE(256, 1024),
    REGION(12, 256),
    CITY(6, 12),
    NEIGHBORHOOD(2, 6),
    STREET(0, 2);

    public static MapScale of(final double scale)
    {
        for (final var value : values())
        {
            if (Doubles.isBetween(scale, value.minimum, value.maximum))
            {
                return value;
            }
        }
        return STATE;
    }

    private final double minimum;

    private final double maximum;

    MapScale(final double minimum, final double maximum)
    {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public boolean at(final MapScale that)
    {
        return this == that;
    }

    public boolean atOrCloserThan(final MapScale that)
    {
        return minimum <= that.minimum;
    }

    public boolean atOrFurtherThan(final MapScale that)
    {
        return minimum >= that.minimum;
    }

    public boolean closerThan(final MapScale that)
    {
        return minimum < that.minimum;
    }

    public boolean furtherThan(final MapScale that)
    {
        return minimum > that.minimum;
    }

    public boolean isZoomedIn(final MapScale that)
    {
        return atOrCloserThan(that);
    }

    public boolean isZoomedOut(final MapScale that)
    {
        return atOrFurtherThan(that);
    }
}