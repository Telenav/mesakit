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

package com.telenav.mesakit.map.geography;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramLocation;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;

/**
 * A vector is a distance (magnitude) at a particular heading. In this case vectors are two-dimensional, which presumes
 * a flat geographic map like a road map.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramLocation.class)
public class Vector
{
    /** The angular direction of the vector */
    private final Heading direction;

    /** The magnitude (length) of the vector */
    private final Distance magnitude;

    public Vector(Distance magnitude)
    {
        this(Heading.NORTHEAST, magnitude);
    }

    public Vector(Heading direction, Distance magnitude)
    {
        this.direction = direction;
        this.magnitude = magnitude;
    }

    @UmlRelation(label = "offsets")
    public Location offset(Location that)
    {
        return that.moved(direction, magnitude);
    }
}
