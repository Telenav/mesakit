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

import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;

public class EdgeAttributes extends GraphElementAttributes<Edge>
{
    // The attributes in this class are shared (from the common data specification) so they need to have the
    // same identifiers in all subclasses. See the superclass of this class for details.

    private static final Lazy<EdgeAttributes> singleton = Lazy.of(EdgeAttributes::new);

    public static EdgeAttributes get()
    {
        return EdgeAttributes.singleton.get();
    }

    public static EdgeAttributes get(final Graph graph)
    {
        return graph.supportedEdgeAttributes();
    }

    public class EdgeAttribute extends GraphElementAttribute
    {
        public EdgeAttribute(final String name)
        {
            super(name);
        }
    }

    public final EdgeAttribute BOUNDS_BOTTOM_LEFT = new EdgeAttribute("BOUNDS_BOTTOM_LEFT");

    public final EdgeAttribute BOUNDS_TOP_RIGHT = new EdgeAttribute("BOUNDS_TOP_RIGHT");

    public final EdgeAttribute BRIDGE_TYPE = new EdgeAttribute("BRIDGE_TYPE");

    public final EdgeAttribute COUNTRY = new EdgeAttribute("COUNTRY");

    public final EdgeAttribute CONNECTIVITY = new EdgeAttribute("CONNECTIVITY");

    public final EdgeAttribute FREE_FLOW_SPEED_CATEGORY = new EdgeAttribute("FREE_FLOW");

    public final EdgeAttribute FROM_VERTEX_IDENTIFIER = new EdgeAttribute("FROM_VERTEX_IDENTIFIER");

    public final EdgeAttribute HOV_LANE_COUNT = new EdgeAttribute("HOV_LANE_COUNT");

    public final EdgeAttribute IS_CLOSED_TO_THROUGH_TRAFFIC = new EdgeAttribute("IS_CLOSED_TO_THROUGH_TRAFFIC");

    public final EdgeAttribute IS_TOLL_ROAD = new EdgeAttribute("IS_TOLL_ROAD");

    public final EdgeAttribute IS_UNDER_CONSTRUCTION = new EdgeAttribute("IS_UNDER_CONSTRUCTION");

    public final EdgeAttribute IS_REVERSE_ONE_WAY = new EdgeAttribute("IS_REVERSE_ONE_WAY");

    public final EdgeAttribute LANE_COUNT = new EdgeAttribute("LANE_COUNT");

    public final EdgeAttribute LENGTH = new EdgeAttribute("LENGTH");

    public final EdgeAttribute FROM_NODE_IDENTIFIER = new EdgeAttribute("FROM_NODE_IDENTIFIER");

    public final EdgeAttribute TO_NODE_IDENTIFIER = new EdgeAttribute("TO_NODE_IDENTIFIER");

    public final EdgeAttribute RELATIONS = new EdgeAttribute("RELATIONS");

    public final EdgeAttribute ROAD_FUNCTIONAL_CLASS = new EdgeAttribute("ROAD_FUNCTIONAL_CLASS");

    public final EdgeAttribute ROAD_NAMES = new EdgeAttribute("ROAD_NAMES");

    public final EdgeAttribute ROAD_SHAPE = new EdgeAttribute("ROAD_SHAPE");

    public final EdgeAttribute ROAD_STATE = new EdgeAttribute("ROAD_STATE");

    public final EdgeAttribute ROAD_SUB_TYPE = new EdgeAttribute("ROAD_SUB_TYPE");

    public final EdgeAttribute ROAD_SURFACE = new EdgeAttribute("ROAD_SURFACE");

    public final EdgeAttribute ROAD_TYPE = new EdgeAttribute("ROAD_TYPE");

    public final EdgeAttribute SPEED_LIMIT = new EdgeAttribute("SPEED_LIMIT");

    public final EdgeAttribute SPEED_LIMIT_IS_METRIC = new EdgeAttribute("SPEED_LIMIT_IS_METRIC");

    public final EdgeAttribute SPEED_PATTERN_IDENTIFIER = new EdgeAttribute("SPEED_PATTERN_IDENTIFIER");

    public final EdgeAttribute WAY_IDENTIFIER_TO_EDGE_IDENTIFIER = new EdgeAttribute("WAY_IDENTIFIERS_TO_IDENTIFIERS");

    public final EdgeAttribute TO_VERTEX_IDENTIFIER = new EdgeAttribute("TO_VERTEX_IDENTIFIER");

    public final EdgeAttribute FORWARD_TMC_IDENTIFIERS = new EdgeAttribute("FORWARD_TMC_IDENTIFIERS");

    public final EdgeAttribute REVERSE_TMC_IDENTIFIERS = new EdgeAttribute("REVERSE_TMC_IDENTIFIERS");

    protected EdgeAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return CommonDataSpecification.get();
    }
}
