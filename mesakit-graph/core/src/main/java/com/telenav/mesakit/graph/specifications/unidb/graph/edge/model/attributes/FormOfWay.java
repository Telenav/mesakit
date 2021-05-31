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

import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public enum FormOfWay implements Quantizable
{
    CONTROLLED_ACCESS(1),
    MULTIPLY_DIGITIZED(2),
    DEFAULT(3),
    ROUNDABOUT(4),
    SPECIAL_TRAFFIC(5),
    CONTROLLED_ACCESS_RAMP(9),
    UNCONTROLLED_ACCESS_RAMP(10),
    FRONTAGE_ROAD(11),
    PARKING_ACCESS(12),
    POI_ACCESS(13),
    PEDESTRIAN_ZONE(14);

    public static long NULL = 0;

    public static FormOfWay forIdentifier(final int identifier)
    {
        for (final var divider : values())
        {
            if (divider.identifier == identifier)
            {
                return divider;
            }
        }
        ensure(false);
        return null;
    }

    private final int identifier;

    FormOfWay(final int identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public long quantum()
    {
        return identifier;
    }
}
