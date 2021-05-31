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

package com.telenav.kivakit.graph.specifications.common.node;

import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.graph.Vertex;
import com.telenav.kivakit.graph.metadata.DataSpecification;
import com.telenav.kivakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.kivakit.graph.specifications.common.element.GraphElementAttributes;

public class NodeAttributes extends GraphElementAttributes<Vertex>
{
    // The attributes in this class are shared (from the common data specification) so they need to have the
    // same identifiers in all subclasses. See the superclass of this class for details.

    private static final Lazy<NodeAttributes> singleton = new Lazy<>(NodeAttributes::new);

    public static NodeAttributes get()
    {
        return singleton.get();
    }

    public class NodeAttribute extends GraphElementAttribute
    {
        public NodeAttribute(final String name)
        {
            super(name);
        }
    }

    public final NodeAttribute NODE_IDENTIFIER = new NodeAttribute("NODE_IDENTIFIER");

    public final NodeAttribute NODE_LOCATION = new NodeAttribute("NODE_LOCATION");

    public final NodeAttribute NODE_IDENTIFIER_TO_INDEX = new NodeAttribute("NODE_IDENTIFIER_TO_INDEX");

    protected NodeAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return CommonDataSpecification.get();
    }
}
