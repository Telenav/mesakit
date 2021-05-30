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


package com.telenav.tdk.graph.specifications.unidb.graph.edge.model;

import com.telenav.tdk.core.kernel.comparison.Differences;

public class UniDbEdgeDifferences
{
    private final UniDbEdge a;

    private final UniDbEdge b;

    public UniDbEdgeDifferences(final UniDbEdge a, final UniDbEdge b)
    {
        this.a = a;
        this.b = b;
    }

    public Differences compare()
    {
        final var differences = new Differences();

        differences.compare("accessType", a.uniDbAccessType(), b.uniDbAccessType());
        differences.compare("adasZCoordinates", a.uniDbAdasZCoordinates(), b.uniDbAdasZCoordinates());
        differences.compare("reverseLaneCount", a.uniDbReverseLaneCount(), b.uniDbReverseLaneCount());
        differences.compare("buildUpArea", a.uniDbIsBuildUpArea(), b.uniDbIsBuildUpArea());
        differences.compare("complexIntersection", a.uniDbIsComplexIntersection(), b.uniDbIsComplexIntersection());
        differences.compare("curvatures", a.uniDbCurvatures(), b.uniDbCurvatures());
        differences.compare("dividedRoad", a.uniDbIsDividedRoad(), b.uniDbIsDividedRoad());
        differences.compare("formOfWay", a.uniDbFormOfWay(), b.uniDbFormOfWay());
        differences.compare("forwardLaneCount", a.uniDbForwardLaneCount(), b.uniDbForwardLaneCount());
        differences.compare("headings", a.uniDbHeadings(), b.uniDbHeadings());
        differences.compare("highwayTag", a.uniDbHighwayType(), b.uniDbHighwayType());
        differences.compare("laneArrows", a.uniDbTurnLaneArrows(), b.uniDbTurnLaneArrows());
        differences.compare("laneDividers", a.uniDbLaneDividers(), b.uniDbLaneDividers());
        differences.compare("laneOneways", a.uniDbLaneOneWays(), b.uniDbLaneOneWays());
        differences.compare("laneTypes", a.uniDbLaneTypes(), b.uniDbLaneTypes());
        differences.compare("leftSideDriving", a.uniDbIsLeftSideDriving(), b.uniDbIsLeftSideDriving());
        differences.compare("overpassUnderpass", a.uniDbOverpassUnderpass(), b.uniDbOverpassUnderpass());
        differences.compare("regionCode", a.uniDbAdasRegionCode(), b.uniDbAdasRegionCode());
        differences.compare("slopes", a.uniDbSlopes(), b.uniDbSlopes());
        differences.compare("speedLimitSource", a.uniDbSpeedLimitSource(), b.uniDbSpeedLimitSource());

        return differences;
    }
}
