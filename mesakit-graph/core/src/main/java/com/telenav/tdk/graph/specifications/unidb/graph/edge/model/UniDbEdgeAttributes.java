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

package com.telenav.kivakit.graph.specifications.unidb.graph.edge.model;

import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.graph.metadata.DataSpecification;
import com.telenav.kivakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.kivakit.graph.specifications.unidb.UniDbDataSpecification;

public class UniDbEdgeAttributes extends EdgeAttributes
{
    private static final Lazy<UniDbEdgeAttributes> singleton = new Lazy<>(UniDbEdgeAttributes::new);

    public static UniDbEdgeAttributes get()
    {
        return singleton.get();
    }

    public class EdgeAttribute extends GraphElementAttribute
    {
        public EdgeAttribute(final String name)
        {
            super(name);
        }
    }

    public final EdgeAttribute ACCESS_TYPE = new EdgeAttribute("ACCESS_TYPE");

    public final EdgeAttribute ADAS_Z_COORDINATES = new EdgeAttribute("ADAS_Z_COORDINATES");

    public final EdgeAttribute REVERSE_LANE_COUNT = new EdgeAttribute("BACKWARD_LANE_COUNT");

    public final EdgeAttribute IS_BUILD_UP_AREA = new EdgeAttribute("BUILD_UP_AREA");

    public final EdgeAttribute IS_COMPLEX_INTERSECTION = new EdgeAttribute("COMPLEX_INTERSECTION");

    public final EdgeAttribute CURVATURES = new EdgeAttribute("CURVATURES");

    public final EdgeAttribute IS_DIVIDED_ROAD = new EdgeAttribute("DIVIDED_ROAD");

    public final EdgeAttribute FORM_OF_WAY = new EdgeAttribute("FORM_OF_WAY");

    public final EdgeAttribute FORWARD_LANE_COUNT = new EdgeAttribute("FORWARD_LANE_COUNT");

    public final EdgeAttribute HEADINGS = new EdgeAttribute("HEADINGS");

    public final EdgeAttribute HIGHWAY_TYPE = new EdgeAttribute("HIGHWAY_TAG");

    public final EdgeAttribute TURN_LANE_ARROWS = new EdgeAttribute("TURN_LANE_ARROWS");

    public final EdgeAttribute LANE_ONE_WAYS = new EdgeAttribute("LANE_ONEWAYS");

    public final EdgeAttribute LANE_TYPES = new EdgeAttribute("LANE_TYPES");

    public final EdgeAttribute LANE_DIVIDERS = new EdgeAttribute("LANE_DIVIDERS");

    public final EdgeAttribute IS_LEFT_SIDE_DRIVING = new EdgeAttribute("LEFT_SIDE_DRIVING");

    public final EdgeAttribute OVERPASS_UNDERPASS = new EdgeAttribute("OVERPASS_UNDERPASS");

    public final EdgeAttribute FORWARD_REFERENCE_SPEED = new EdgeAttribute("FORWARD_REFERENCE_SPEED");

    public final EdgeAttribute REVERSE_REFERENCE_SPEED = new EdgeAttribute("REVERSE_REFERENCE_SPEED");

    public final EdgeAttribute FORWARD_REGION_CODE = new EdgeAttribute("FORWARD_REGION_CODES");

    public final EdgeAttribute REVERSE_REGION_CODE = new EdgeAttribute("REVERSE_REGION_CODES");

    public final EdgeAttribute ROUTE_TYPE = new EdgeAttribute("ROUTE_TYPE");

    public final EdgeAttribute SPEED_LIMIT_SOURCE = new EdgeAttribute("SPEED_LIMIT_SOURCE");

    public final EdgeAttribute SLOPES = new EdgeAttribute("SLOPES");

    protected UniDbEdgeAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return UniDbDataSpecification.get();
    }
}
