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

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;

public class RelationAttributes extends GraphElementAttributes<EdgeRelation>
{
    // The attributes in this class are shared (from the common data specification) so they need to have the
    // same identifiers in all subclasses. See the superclass of this class for details.

    private static final Lazy<RelationAttributes> singleton = Lazy.of(RelationAttributes::new);

    public static RelationAttributes get()
    {
        return singleton.get();
    }

    public class RelationAttribute extends GraphElementAttribute
    {
        public RelationAttribute(final String name)
        {
            super(name);
        }
    }

    public final RelationAttribute TYPE = new RelationAttribute("TYPE");

    public final RelationAttribute MEMBER_IDENTIFIERS = new RelationAttribute("MEMBER_IDENTIFIERS");

    public final RelationAttribute MEMBER_ROLES = new RelationAttribute("MEMBER_ROLES");

    public final RelationAttribute VIA_NODE_LOCATION = new RelationAttribute("VIA_NODE_LOCATIONS");

    public final RelationAttribute MAP_IDENTIFIER = new RelationAttribute("MAP_IDENTIFIERS");

    protected RelationAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return CommonDataSpecification.get();
    }
}
