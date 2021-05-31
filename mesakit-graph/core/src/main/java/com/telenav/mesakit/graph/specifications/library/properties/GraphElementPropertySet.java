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

import com.telenav.mesakit.graph.GraphElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GraphElementPropertySet<T extends GraphElement> implements Iterable<GraphElementProperty<T>>
{
    private final Set<GraphElementProperty<T>> properties = new HashSet<>();

    public GraphElementPropertySet<T> add(final GraphElementPropertySet<T> that)
    {
        for (final var at : that)
        {
            add(at);
        }
        return this;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<GraphElementProperty<T>> iterator()
    {
        return properties().iterator();
    }

    public List<GraphElementProperty<T>> properties()
    {
        final var properties = new ArrayList<>(this.properties);
        Collections.sort(properties);
        return properties;
    }

    protected void add(final GraphElementProperty<T> property)
    {
        this.properties.add(property);
    }
}
