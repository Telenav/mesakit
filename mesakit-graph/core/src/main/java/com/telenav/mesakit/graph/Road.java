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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.RoadState;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadType;

/**
 * A road may be a PBF way or a simple graph edge
 *
 * @author songg
 */
public interface Road
{
    Route asRoute();

    Vertex from();

    boolean isValid();

    Count laneCount();

    Distance length();

    Road reversed();

    RoadFunctionalClass roadFunctionalClass();

    RoadName roadName();

    RoadName roadName(final RoadName.Type type);

    Polyline roadShape();

    RoadState roadState();

    RoadSubType roadSubType();

    RoadType roadType();

    Vertex to();

    PbfWayIdentifier wayIdentifier();
}
