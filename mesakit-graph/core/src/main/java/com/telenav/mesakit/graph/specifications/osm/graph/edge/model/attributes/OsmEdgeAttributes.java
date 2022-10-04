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

package com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;

public class OsmEdgeAttributes extends EdgeAttributes
{
    private static final Lazy<OsmEdgeAttributes> singleton = Lazy.lazy(OsmEdgeAttributes::new);

    public static OsmEdgeAttributes get()
    {
        return OsmEdgeAttributes.singleton.get();
    }

    public class OsmEdgeAttribute extends GraphElementAttribute
    {
        public OsmEdgeAttribute(String name)
        {
            super(name);
        }
    }

    public final OsmEdgeAttribute GRADE_SEPARATION_LEVEL = new OsmEdgeAttribute("GRADE_SEPARATION_LEVEL");

    public final OsmEdgeAttribute IS_DOUBLE_DIGITIZED = new OsmEdgeAttribute("IS_DOUBLE_DIGITIZED");

    public final OsmEdgeAttribute RAW_IDENTIFIER = new OsmEdgeAttribute("RAW_IDENTIFIER");

    public final OsmEdgeAttribute FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER = new OsmEdgeAttribute("FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER");

    public final OsmEdgeAttribute REVERSE_TELENAV_TRAFFIC_LOCATION_IDENTIFIER = new OsmEdgeAttribute("REVERSE_TELENAV_TRAFFIC_LOCATION_IDENTIFIER");

    public final OsmEdgeAttribute FORWARD_TRACE_COUNT = new OsmEdgeAttribute("FORWARD_TRACE_COUNT");

    public final OsmEdgeAttribute REVERSE_TRACE_COUNT = new OsmEdgeAttribute("REVERSE_TRACE_COUNT");

    protected OsmEdgeAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return OsmDataSpecification.get();
    }
}
