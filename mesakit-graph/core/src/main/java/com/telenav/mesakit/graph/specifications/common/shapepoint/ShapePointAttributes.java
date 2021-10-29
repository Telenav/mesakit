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

package com.telenav.mesakit.graph.specifications.common.shapepoint;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.common.node.NodeAttributes;

public class ShapePointAttributes extends NodeAttributes
{
    private static final Lazy<ShapePointAttributes> singleton = Lazy.of(ShapePointAttributes::new);

    public static ShapePointAttributes get()
    {
        return singleton.get();
    }

    public class ShapePointAttribute extends GraphElementAttribute
    {
        public ShapePointAttribute(String name)
        {
            super(name);
        }
    }

    public ShapePointAttribute ATTRIBUTE_IS_CLIPPED = new ShapePointAttribute("IS_CLIPPED");

    protected ShapePointAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return CommonDataSpecification.get();
    }
}
