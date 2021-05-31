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

package com.telenav.mesakit.graph.specifications.common.edge;

import com.telenav.kivakit.kernel.data.comparison.Differences;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.road.model.RoadName;

public class EdgeDifferences extends Differences
{
    private final Edge a;

    private final Edge b;

    public EdgeDifferences(final Edge a, final Edge b)
    {
        this.a = a;
        this.b = b;
    }

    public Differences compare()
    {
        final var differences = new Differences();

        // Add differences that are meaningful for all data specifications
        differences.compare("freeFlow", a.freeFlowSpeed(), b.freeFlowSpeed());
        differences.compare("from", a.from().location(), b.from().location());
        differences.compare("identifier", a.mapEdgeIdentifier(), b.mapEdgeIdentifier());
        differences.compare("laneCount", a.laneCount(), b.laneCount());
        differences.compare("length", a.length(), b.length());
        differences.compare("roadFunctionalClass", a.roadFunctionalClass(), b.roadFunctionalClass());
        differences.compare("roadName", a.roadName(), b.roadName());
        differences.compare("roadNameAlternate", a.roadName(RoadName.Type.ALTERNATE),
                b.roadName(RoadName.Type.ALTERNATE));
        differences.compare("roadNameExit", a.roadName(RoadName.Type.EXIT), b.roadName(RoadName.Type.EXIT));
        differences.compare("roadNameOfficial", a.roadName(RoadName.Type.OFFICIAL),
                b.roadName(RoadName.Type.OFFICIAL));
        differences.compare("roadNameRoute", a.roadName(RoadName.Type.ROUTE),
                b.roadName(RoadName.Type.ROUTE));
        differences.compare("roadShape", a.roadShape(), b.roadShape());
        differences.compare("roadSubType", a.roadSubType(), b.roadSubType());
        differences.compare("roadType", a.roadType(), b.roadType());
        differences.compare("routeType", a.uniDbRouteType(), b.uniDbRouteType());
        differences.compare("to", a.to().location(), b.to().location());
        differences.compare("type", a.type(), b.type());
        differences.compare("tmcIdentifiers", a.tmcIdentifiers(), b.tmcIdentifiers());

        return differences;
    }
}
