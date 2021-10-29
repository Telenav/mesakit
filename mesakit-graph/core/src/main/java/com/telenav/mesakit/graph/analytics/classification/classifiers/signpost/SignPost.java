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

package com.telenav.mesakit.graph.analytics.classification.classifiers.signpost;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.library.matchers.Matchers;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import static com.telenav.mesakit.map.measurements.geographic.Angle._180_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle.degrees;

public class SignPost
{
    private final Vertex vertex;

    private final EdgeSet outEdges;

    public SignPost(Edge ramp)
    {
        vertex = ramp.from();
        outEdges = ramp.from().outEdges();
    }

    public SignPost(Vertex vertex, EdgeSet outEdges)
    {
        this.vertex = vertex;
        this.outEdges = outEdges;
    }

    public boolean hasDestinations()
    {
        for (var outEdge : outEdges)
        {
            var edgeHighwayTag = outEdge.tagValue("highway");
            if (edgeHighwayTag == null || edgeHighwayTag.isEmpty())
            {
                return false;
            }
            var destinationTag = outEdge.tagValue("destination");
            if (destinationTag != null && !destinationTag.isEmpty())
            {
                continue;
            }
            var destinationRefTag = outEdge.tagValue("destination:ref");
            if (destinationRefTag != null && !destinationRefTag.isEmpty())
            {
                continue;
            }
            var destinationLanesTag = outEdge.tagValue("destination:lanes");
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
        var iterator = outEdges.iterator();

        var outEdgeShapePoints1 = iterator.next().roadShape();
        var outEdgeHeading1 = outEdgeShapePoints1.firstSegment().length().isGreaterThan(Distance.meters(20))
                ? outEdgeShapePoints1.initialHeading()
                : outEdgeShapePoints1.start().headingTo(outEdgeShapePoints1.end());

        var outEdgeShapePoints2 = iterator.next().roadShape();
        var outEdgeHeading2 = outEdgeShapePoints2.firstSegment().length().isGreaterThan(Distance.meters(20))
                ? outEdgeShapePoints2.initialHeading()
                : outEdgeShapePoints2.start().headingTo(outEdgeShapePoints2.end());

        return outEdgeHeading1.difference(outEdgeHeading2, Chirality.SMALLEST).isLessThan(degrees(45));
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
            var iterator = outEdges.iterator();
            var first = iterator.next();
            var second = iterator.next();
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
            var iterator = outEdges.iterator();
            var first = iterator.next();
            var second = iterator.next();
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

    private boolean isLeftBranchFirst(Edge first, Edge second)
    {
        return first.initialHeading().difference(second.initialHeading(), Chirality.CLOCKWISE)
                .isLessThan(_180_DEGREES);
    }
}
