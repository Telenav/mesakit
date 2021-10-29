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

package com.telenav.mesakit.graph.specifications.common.vertex;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementProperties;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;

public class VertexProperties extends GraphElementProperties<Vertex>
{
    private static final Lazy<VertexProperties> singleton = Lazy.of(VertexProperties::new);

    public static VertexProperties get()
    {
        return singleton.get();
    }

    public abstract class VertexProperty extends com.telenav.mesakit.graph.specifications.library.properties.GraphElementProperty<Vertex>
    {
        protected VertexProperty(String name, Attribute<?> attribute)
        {
            super(name, attribute, CommonDataSpecification.get());
            add(this);
        }
    }

    public final VertexProperty GRADE_SEPARATION = new VertexProperty("grade-separation-level", VertexAttributes.get().GRADE_SEPARATION)
    {
        @Override
        public Object value(Vertex vertex)
        {
            if (vertex.supports(VertexAttributes.get().GRADE_SEPARATION))
            {
                return vertex.gradeSeparation();
            }
            return null;
        }
    };

    public final VertexProperty IN_EDGES = new VertexProperty("in-edges", VertexAttributes.get().IN_EDGE_COUNT)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.inEdges();
        }
    };

    public final VertexProperty OUT_EDGES = new VertexProperty("out-edges", VertexAttributes.get().OUT_EDGE_COUNT)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.outEdges();
        }
    };

    public final VertexProperty IS_DEAD_END = new VertexProperty("is-dead-end", VertexAttributes.get().NONE)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.isDeadEnd();
        }
    };

    public final VertexProperty IS_CLIPPED = new VertexProperty("is-clipped", VertexAttributes.get().IS_CLIPPED)
    {
        @Override
        public Object value(Vertex vertex)
        {
            if (vertex.supports(VertexAttributes.get().IS_CLIPPED))
            {
                return vertex.isClipped();
            }
            return false;
        }
    };

    public final VertexProperty IS_SYNTHETIC = new VertexProperty("is-synthetic", VertexAttributes.get().IS_SYNTHETIC)
    {
        @Override
        public Object value(Vertex vertex)
        {
            if (vertex.supports(VertexAttributes.get().IS_SYNTHETIC))
            {
                return vertex.isSynthetic();
            }
            return false;
        }
    };

    public final VertexProperty IS_DECISION_POINT = new VertexProperty("is-decision-point", VertexAttributes.get().NONE)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.isDecisionPoint();
        }
    };

    public final VertexProperty IS_ON_FREEWAY = new VertexProperty("is-on-freeway", VertexAttributes.get().NONE)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.isOnFreeway();
        }
    };

    public final VertexProperty IS_INTERSECTION = new VertexProperty("is-intersection", VertexAttributes.get().NONE)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.isIntersection();
        }
    };

    public final VertexProperty IS_THROUGH_NODE = new VertexProperty("is-through-vertex", VertexAttributes.get().NONE)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.isThroughVertex();
        }
    };

    public final VertexProperty LOCATION = new VertexProperty("location", VertexAttributes.get().NODE_LOCATION)
    {
        @Override
        public Object value(Vertex vertex)
        {
            return vertex.location();
        }
    };

    protected VertexProperties()
    {
    }
}
