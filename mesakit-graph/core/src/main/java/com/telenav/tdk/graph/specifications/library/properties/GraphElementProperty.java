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

package com.telenav.kivakit.graph.specifications.library.properties;

import com.telenav.kivakit.kernel.language.string.Strings;
import com.telenav.kivakit.graph.GraphElement;
import com.telenav.kivakit.graph.metadata.DataSpecification;
import com.telenav.kivakit.graph.specifications.library.attributes.Attribute;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public abstract class GraphElementProperty<T extends GraphElement> implements Comparable<GraphElementProperty<T>>
{
    private final String name;

    private final DataSpecification specification;

    private final Attribute<?> attribute;

    protected GraphElementProperty(final String name, final Attribute<?> attribute,
                                   final DataSpecification specification)
    {
        ensure(name != null);
        ensure(specification != null);

        this.name = name;
        this.attribute = attribute;
        this.specification = specification;
    }

    public Attribute<?> attribute()
    {
        return this.attribute;
    }

    @Override
    public int compareTo(final GraphElementProperty<T> that)
    {
        return this.name.compareTo(that.name);
    }

    public boolean matches(final DataSpecification specification)
    {
        return this.specification.type().matches(specification.type());
    }

    public String name()
    {
        return Strings.camelToHyphenated(this.name);
    }

    @SuppressWarnings("unchecked")
    public String string(final Object element)
    {
        if (element != null)
        {
            final var value = value((T) element);
            if (value != null)
            {
                return this.name + ": " + value;
            }
        }
        return this.name + ": null";
    }

    @SuppressWarnings("unchecked")
    public Object valueFromObject(final Object element)
    {
        return value((T) element);
    }

    protected abstract Object value(T element);
}
