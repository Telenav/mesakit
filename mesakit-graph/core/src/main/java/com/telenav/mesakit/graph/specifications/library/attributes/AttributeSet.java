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

package com.telenav.mesakit.graph.specifications.library.attributes;

import java.util.HashSet;
import java.util.Set;

public class AttributeSet
{
    public static AttributeSet of(Attribute<?>... attributes)
    {
        var set = new AttributeSet();
        for (var attribute : attributes)
        {
            set.add(attribute);
        }
        return set;
    }

    private final Set<Attribute<?>> attributes = new HashSet<>();

    public void add(Attribute<?> attribute)
    {
        attributes.add(attribute);
    }

    public synchronized Set<Attribute<?>> attributes()
    {
        return attributes;
    }

    public boolean contains(Attribute<?> attribute)
    {
        return attributes.contains(attribute);
    }
}
