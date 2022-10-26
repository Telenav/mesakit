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

import com.telenav.kivakit.collections.map.TwoWayMap;

import java.util.Objects;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

/**
 * An attribute of an {@link AttributeStore}, having a name and an automatically assigned identifier.
 *
 * @author jonathanl (shibo)
 */
public class Attribute<T> implements Comparable<Attribute<T>>
{
    private static int nextIdentifier = 1;

    private static final TwoWayMap<String, Integer> nameToIdentifier = new TwoWayMap<>();

    private final String name;

    private final int identifier;

    protected Attribute(String name)
    {
        this.name = name;

        synchronized (Attribute.class)
        {
            var identifier = nameToIdentifier.get(name);
            if (identifier == null)
            {
                identifier = nextIdentifier++;
                var existingAttribute = nameToIdentifier.key(identifier);
                ensure(existingAttribute == null || name.equalsIgnoreCase(existingAttribute),
                        "The attribute $ already has identifier $", existingAttribute, identifier);
                nameToIdentifier.put(name, identifier);
            }
            this.identifier = identifier;
        }
    }

    @Override
    public int compareTo(Attribute<T> that)
    {
        return toString().compareTo(that.toString());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Attribute<?> that)
        {
            return identifier == that.identifier;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(identifier);
    }

    /**
     * Returns the attribute's identifier. This is a value from 1 to n, where n is the total number of attributes. This
     * identifier is used by the attribute loader as an index to determine what attributes are supported.
     */
    public int identifier()
    {
        return identifier;
    }

    /**
     * Returns the name of the attribute
     */
    public String name()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name() + " [" + identifier + "]";
    }
}
