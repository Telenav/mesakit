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

package com.telenav.aonia.map.data.formats.pbf.unidb;

import com.telenav.aonia.map.data.formats.pbf.model.tags.PbfTagMap;

public class UniDbRelationTag
{
    public static final String KEY_TYPE = "type";

    public static final String TYPE_RESTRICTION = "restriction";

    public static final String TYPE_ONEWAY = "oneway";

    public static final String TYPE_BARRIER = "barrier";

    public static final String TYPE_DIVIDED_JUNCTION = "divided_junction";

    public static final String TYPE_BIFURCATION = "bifurcation";

    public static final String TYPE_LANE_CONNECTIVITY = "lane_connectivity";

    public static final String TYPE_SIGNPOST = "signpost";

    public static final String TYPE_NATURAL_GUIDANCE = "natural_guidance";

    public static final String TYPE_JUNCTION_VIEW = "junction_view";

    public static final String TYPE_GENERIC_JUNCTION_VIEW = "gjv";

    public static final String TYPE_CONSTRUCTION = "construction";

    public static final String TYPE_TRAFFIC_SIGNALS = "traffic_signals";

    public static final String TYPE_TRAFFIC_SIGN = "traffic_sign";

    public static final String TYPE_SPEED_CAMERA = "speed_camera";

    public static final String TYPE_SAFETY_CAMERA = "safety_camera";

    public static final String TYPE_ADAS_NODE = "adas_node";

    public static final String TYPE_ADAS_MAXSPEED = "adas:maxspeed";

    public static final String TYPE_DIR_SLOPE = "dir_slope";

    public static boolean isAdasRelations(final PbfTagMap tags)
    {
        final var type = tags.get(KEY_TYPE);
        return TYPE_ADAS_NODE.equals(type) || TYPE_ADAS_MAXSPEED.equals(type);
    }

    public static boolean isJunctionView(final PbfTagMap tags)
    {
        final var type = tags.get(KEY_TYPE);
        return TYPE_JUNCTION_VIEW.equals(type) || TYPE_GENERIC_JUNCTION_VIEW.equals(type);
    }

    public static boolean isTrafficCamera(final PbfTagMap tags)
    {
        final var type = tags.get(KEY_TYPE);
        return TYPE_SAFETY_CAMERA.equals(type) || TYPE_SPEED_CAMERA.equals(type);
    }
}
