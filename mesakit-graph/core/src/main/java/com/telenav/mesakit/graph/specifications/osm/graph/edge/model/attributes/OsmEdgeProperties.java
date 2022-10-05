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
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeProperties;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;

public class OsmEdgeProperties extends EdgeProperties
{
    private static final Lazy<OsmEdgeProperties> singleton = Lazy.lazy(OsmEdgeProperties::new);

    public static OsmEdgeProperties get()
    {
        return OsmEdgeProperties.singleton.get();
    }

    public abstract class OsmEdgeProperty extends EdgeProperty
    {
        protected OsmEdgeProperty(String name, Attribute<?> attribute)
        {
            super(name, attribute);
            add(this);
        }
    }

    public final OsmEdgeProperty IS_DOUBLE_DIGITIZED = new OsmEdgeProperty("double-digitized", OsmEdgeAttributes.get().IS_DOUBLE_DIGITIZED)
    {
        @Override
        public Object value(Edge edge)
        {
            return edge.osmIsDoubleDigitized();
        }
    };

    public final EdgeProperty IS_DESTINATION_TAGGED = new EdgeProperty("is-osm-destination-tagged", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(Edge edge)
        {
            return edge.osmIsDestinationTagged();
        }
    };

    protected OsmEdgeProperties()
    {
    }
}
