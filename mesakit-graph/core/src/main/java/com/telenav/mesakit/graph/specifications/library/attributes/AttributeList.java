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

import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Maintains a list of attributes in alphabetically sorted order to make reading them easier.
 */
public class AttributeList implements Iterable<Attribute<?>>
{
    private static final Map<String, Attribute<?>> nameToAttribute = new HashMap<>();

    private ObjectList<Attribute<?>> attributes;

    private boolean sorted;

    public AttributeList(ObjectList<Attribute<?>> attributes)
    {
        this.attributes = attributes;
    }

    public AttributeList()
    {
        attributes = new ObjectList<>();
    }

    /**
     * Adds an attribute to the list, but uniques the attribute by name. If an attribute of the same name has already
     * been added to some list, then that attribute will be returned so that reference comparisons can be done for the
     * sake of efficiency.
     */
    public Attribute<?> add(Attribute<?> attribute)
    {
        // If an attribute with the same name already exists,
        var existing = nameToAttribute.get(attribute.name());
        if (existing != null)
        {

            // then use that attribute so we can do reference comparisons
            attribute = existing;
        }
        else
        {

            // otherwise add the attribute to the map
            nameToAttribute.put(attribute.name(), attribute);
        }

        // Add the attribute
        attributes.add(attribute);

        // and flag the list as unsorted
        sorted = false;

        return attribute;
    }

    public void addAll(AttributeList that)
    {
        addAll(that.attributes);
    }

    public void addAll(List<Attribute<?>> list)
    {
        list.forEach(this::add);
    }

    /**
     * @return The attributes, sorted alphabetically
     */
    public synchronized ObjectList<Attribute<?>> attributes()
    {
        if (!sorted)
        {
            sorted = true;
            attributes = attributes.sorted();
        }
        return attributes;
    }

    public <T> boolean contains(Attribute<T> attribute)
    {
        return attributes.contains(attribute);
    }

    public boolean contains(String attributeName)
    {
        return nameToAttribute.containsKey(attributeName);
    }

    @NotNull
    @Override
    public Iterator<Attribute<?>> iterator()
    {
        return attributes.iterator();
    }

    public String join(String separator)
    {
        return attributes.join(separator);
    }

    public void remove(Attribute<?> attribute)
    {
        attributes.remove(attribute);
        nameToAttribute.remove(attribute.name());
    }
}
