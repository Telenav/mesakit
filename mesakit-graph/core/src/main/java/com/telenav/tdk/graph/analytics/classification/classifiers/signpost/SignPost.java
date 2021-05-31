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

package com.telenav.kivakit.graph.analytics.classification.classifiers.signpost;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.Vertex;
import com.telenav.kivakit.graph.collections.EdgeSet;
import com.telenav.kivakit.graph.library.matchers.Matchers;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.measurements.Angle;
import com.telenav.kivakit.map.measurements.Angle.Chirality;
import com.telenav.kivakit.map.measurements.Distance;

public class SignPost
{
    private final Vertex vertex;

    private final EdgeSet outEdges;

    public SignPost(final Edge ramp)
    {
        vertex = ramp.from();
        outEdges = ramp.from().outEdges();
    }

    public SignPost(final Vertex vertex, final EdgeSet outEdges)
    {
        this.vertex = vertex;
        this.outEdges = outEdges;
    }

    public boolean hasDestinations()
    {
        for (final var outEdge : outEdges)
        {
            final var edgeHighwayTag = outEdge.tagValue("highway");
            if (edgeHighwayTag == null || edgeHighwayTag.isEmpty())
            {
                return false;
            }
            final var destinationTag = outEdge.tagValue("destination");
            if (destinationTag != null && !destinationTag.isEmpty())
            {
                continue;
            }
            final var destinationRefTag = outEdge.tagValue("destination:ref");
            if (destinationRefTag != null && !destinationRefTag.isEmpty())
            {
                continue;
            }
            final var destinationLanesTag = outEdge.tagValue("destination:lanes");
            if (destinationLanesTag != null && !destinationLanesTag.isEmpty())
            {
                continue;
            }
            return false;
        }
        return true;
    }

    public boolean hasProperAngle()
    {
        final var iterator = outEdges.iterator();

        final var outEdgeShapepoints1 = iterator.next().roadShape();
        final var outEdgeHeading1 = outEdgeShapepoints1.firstSegment().length().isGreaterThan(Distance.meters(20))
                ? outEdgeShapepoints1.initialHeading()
                : outEdgeShapepoints1.start().headingTo(outEdgeShapepoints1.end());

        final var outEdgeShapepoints2 = iterator.next().roadShape();
        final var outEdgeHeading2 = outEdgeShapepoints2.firstSegment().length().isGreaterThan(Distance.meters(20))
                ? outEdgeShapepoints2.initialHeading()
                : outEdgeShapepoints2.start().headingTo(outEdgeShapepoints2.end());

        return outEdgeHeading1.difference(outEdgeHeading2, Chirality.SMALLEST).isLessThan(Angle.degrees(45));
    }

    public boolean isExit()
    {
        var tag = vertex.tagValue("exit_to");
        if (tag != null && !tag.isEmpty())
        {
            return true;
        }
        tag = vertex.tagValue("exit_to:left");
        if (tag != null && !tag.isEmpty())
        {
            return true;
        }
        tag = vertex.tagValue("exit_to:right");
        return tag != null && !tag.isEmpty();
    }

    public boolean isMotorwayJunction()
    {
        return "motorway_junction".equalsIgnoreCase(vertex.tagValue("highway"));
    }

    public boolean isOnFreeway()
    {
        return outEdges.hasMatch(Matchers.FREEWAYS_WITHOUT_RAMPS);
    }

    public Edge leftBranch()
    {
        // Distinguish left or right only when there are two branches
        if (outEdges.size() == 2)
        {
            final var iterator = outEdges.iterator();
            final var first = iterator.next();
            final var second = iterator.next();
            return isLeftBranchFirst(first, second) ? first : second;
        }
        return null;
    }

    public Location location()
    {
        return outEdges.first().from().location();
    }

    public EdgeSet outEdges()
    {
        return outEdges;
    }

    public Edge rightBranch()
    {
        // Distinguish left or right only when there are two branches
        if (outEdges.size() == 2)
        {
            final var iterator = outEdges.iterator();
            final var first = iterator.next();
            final var second = iterator.next();
            return isLeftBranchFirst(first, second) ? second : first;
        }
        return null;
    }

    @Override
    public String toString()
    {
        return location().toString();
    }

    public Vertex vertex()
    {
        return vertex;
    }

    private boolean isLeftBranchFirst(final Edge first, final Edge second)
    {
        return first.initialHeading().difference(second.initialHeading(), Chirality.CLOCKWISE)
                .isLessThan(Angle._180_DEGREES);
    }
}
