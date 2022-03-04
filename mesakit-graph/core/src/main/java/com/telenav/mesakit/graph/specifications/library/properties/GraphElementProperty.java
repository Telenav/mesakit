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

package com.telenav.mesakit.graph.specifications.library.properties;

import com.telenav.kivakit.core.string.CaseFormat;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

public abstract class GraphElementProperty<T extends GraphElement> implements Comparable<GraphElementProperty<T>>
{
    private final String name;

    private final DataSpecification specification;

    private final Attribute<?> attribute;

    protected GraphElementProperty(String name, Attribute<?> attribute,
                                   DataSpecification specification)
    {
        ensure(name != null);
        ensure(specification != null);

        this.name = name;
        this.attribute = attribute;
        this.specification = specification;
    }

    public Attribute<?> attribute()
    {
        return attribute;
    }

    @Override
    public int compareTo(GraphElementProperty<T> that)
    {
        return name.compareTo(that.name);
    }

    public boolean matches(DataSpecification specification)
    {
        return this.specification.type().matches(specification.type());
    }

    public String name()
    {
        return CaseFormat.camelCaseToHyphenated(name);
    }

    @SuppressWarnings("unchecked")
    public String string(Object element)
    {
        if (element != null)
        {
            var value = value((T) element);
            if (value != null)
            {
                return name + ": " + value;
            }
        }
        return name + ": null";
    }

    @SuppressWarnings("unchecked")
    public Object valueFromObject(Object element)
    {
        return value((T) element);
    }

    protected abstract Object value(T element);
}
