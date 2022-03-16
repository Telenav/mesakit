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

package com.telenav.mesakit.graph.specifications.common.relation;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementProperty;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;

public class RelationProperties extends GraphElementPropertySet<EdgeRelation>
{
    private static final Lazy<RelationProperties> singleton = Lazy.of(RelationProperties::new);

    public static RelationProperties get()
    {
        return singleton.get();
    }

    public abstract class RelationProperty extends GraphElementProperty<EdgeRelation>
    {
        protected RelationProperty(String name, Attribute<?> attribute)
        {
            super(name, attribute, CommonDataSpecification.get());
            add(this);
        }
    }

    public final RelationProperty MEMBERS = new RelationProperty("members", RelationAttributes.get().MEMBER_IDENTIFIERS)
    {
        @Override
        public Object value(EdgeRelation relation)
        {
            return relation.members();
        }
    };

    public final RelationProperty TYPE = new RelationProperty("type", RelationAttributes.get().TYPE)
    {
        @Override
        public Object value(EdgeRelation relation)
        {
            var type = relation.type();
            return type == null ? "N/A" : type.toString();
        }
    };

    protected RelationProperties()
    {
    }
}
