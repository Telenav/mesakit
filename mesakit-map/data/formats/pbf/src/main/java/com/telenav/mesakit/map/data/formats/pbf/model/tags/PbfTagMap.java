////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.model.tags;

import com.telenav.kivakit.core.kernel.interfaces.collection.Keyed;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.kivakit.core.kernel.language.primitives.Longs;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelTags;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

/**
 * A map between tag keys and values.
 * <p>
 * HOTSPOT: This class has been identified as a hotpot by YourKit and so it has been optimized to avoid allocating
 * {@link HashMap} instances for smaller tag maps. The average number of tags per entity in OSM is between 2.3 and 3.95,
 * so 8 fields should cover the majority of OSM tag maps.
 */
@SuppressWarnings("StaticInitializerReferencesSubClass")
@UmlClassDiagram(diagram = DiagramPbfModelTags.class)
@UmlRelation(label = "stores", referent = Tag.class)
@UmlExcludeSuperTypes(Iterable.class)
public class PbfTagMap implements Iterable<Tag>, Keyed<String, String>
{
    /** Empty tag map */
    public static final PbfTagMap EMPTY = new EmptyPbfTagMap();

    private static final int FIELDS = 8;

    public static PbfTagMap create()
    {
        return new PbfTagMap(1);
    }

    public static PbfTagMap from(final Collection<Tag> tags)
    {
        if (tags.isEmpty())
        {
            return EMPTY;
        }
        final var map = new PbfTagMap(tags.size());
        for (final var tag : tags)
        {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }

    public static PbfTagMap from(final Collection<Tag> tags, final PbfTagFilter filter)
    {
        if (tags.isEmpty())
        {
            return EMPTY;
        }
        final var map = new PbfTagMap(tags.size());
        for (final var tag : tags)
        {
            if (filter.accepts(tag))
            {
                map.put(tag.getKey(), tag.getValue());
            }
        }
        return map;
    }

    // Full hashmap when there are more than FIELDS tags
    private Map<String, String> tags;

    // The size of this map
    private int size;

    // Simple fields when the size <= FIELDS
    private String key0, value0;

    private String key1, value1;

    private String key2, value2;

    private String key3, value3;

    private String key4, value4;

    private String key5, value5;

    private String key6, value6;

    private String key7, value7;

    protected PbfTagMap(final int initialCapacity)
    {
        if (initialCapacity > 8)
        {
            // Allocate with initial capacity to prevent resizing at 75% occupancy
            tags = new HashMap<>(initialCapacity * 4 / 3);
        }
    }

    public boolean containsKey(final String key)
    {
        if (isCompact())
        {
            for (var i = 0; i < size(); i++)
            {
                if (key.equals(compactKey(i)))
                {
                    return true;
                }
            }
        }
        else
        {
            return tags.containsKey(key);
        }
        return false;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof PbfTagMap)
        {
            final var that = (PbfTagMap) object;
            if (size() == that.size())
            {
                final var keys = keys();
                while (keys.hasNext())
                {
                    final var at = keys.next();
                    if (!Strings.equals(that.get(at), get(at)))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String get(final String key)
    {
        if (isCompact())
        {
            if (key0 == null)
            {
                return null;
            }
            if (key.equals(key0))
            {
                return value0;
            }

            if (key1 == null)
            {
                return null;
            }
            if (key.equals(key1))
            {
                return value1;
            }

            if (key2 == null)
            {
                return null;
            }
            if (key.equals(key2))
            {
                return value2;
            }

            if (key3 == null)
            {
                return null;
            }
            if (key.equals(key3))
            {
                return value3;
            }

            if (key4 == null)
            {
                return null;
            }
            if (key.equals(key4))
            {
                return value4;
            }

            if (key5 == null)
            {
                return null;
            }
            if (key.equals(key5))
            {
                return value5;
            }

            if (key6 == null)
            {
                return null;
            }
            if (key.equals(key6))
            {
                return value6;
            }

            if (key7 == null)
            {
                return null;
            }
            if (key.equals(key7))
            {
                return value7;
            }

            return null;
        }
        else
        {
            return tags.get(key);
        }
    }

    public String get(final String key, final String defaultValue)
    {
        final var value = get(key);
        if (value == null)
        {
            return defaultValue;
        }
        return value;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(
                key0, value0,
                key1, value1,
                key2, value2,
                key3, value3,
                key4, value4,
                key5, value5,
                key6, value6,
                key7, value7,
                tags);
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<Tag> iterator()
    {
        return new Iterator<>()
        {
            private final Iterator<String> keys = keys();

            @Override
            public boolean hasNext()
            {
                return keys.hasNext();
            }

            @Override
            public Tag next()
            {
                final var at = keys.next();
                return new Tag(at, get(at));
            }
        };
    }

    public Iterator<String> keys()
    {
        if (isCompact())
        {
            return new Iterator<>()
            {
                private int index;

                @Override
                public boolean hasNext()
                {
                    return index < size();
                }

                @Override
                public String next()
                {
                    return compactKey(index++);
                }
            };
        }
        else
        {
            return tags.keySet().iterator();
        }
    }

    public void put(final String key, final String value)
    {
        if (isCompact())
        {
            for (var i = 0; i < size(); i++)
            {
                if (key.equals(compactKey(i)))
                {
                    setField(i, key, value);
                    return;
                }
            }
        }

        // Increase the size of the map
        capacity(size() + 1);

        // and if it's still compact
        if (isCompact())
        {
            // put the key value pair into fields
            setField(size++, key, value);
        }
        else
        {
            // otherwise store it in a hashmap
            tags.put(key, value);
        }
    }

    public void putAll(final Iterable<Tag> tags)
    {
        for (final var tag : tags)
        {
            put(tag.getKey(), tag.getValue());
        }
    }

    public int size()
    {
        return tags == null ? size : tags.size();
    }

    public Tag tag(final String key)
    {
        return new Tag(key, get(key));
    }

    public String value(final String key)
    {
        return get(key);
    }

    public int valueAsInt(final String key)
    {
        return Ints.parse(get(key));
    }

    public long valueAsLong(final String key)
    {
        return Longs.parse(get(key));
    }

    public int valueAsNaturalNumber(final String key)
    {
        return Ints.parseNaturalNumber(get(key));
    }

    public boolean valueIsNo(final String key)
    {
        final var value = value(key);
        if (value != null)
        {
            return "no".equals(value);
        }
        return false;
    }

    public boolean valueIsYes(final String key)
    {
        final var value = value(key);
        if (value != null)
        {
            return "yes".equals(value);
        }
        return false;
    }

    public StringList valueSplit(final String key)
    {
        final var split = new StringList();
        final var value = get(key);
        if (value != null)
        {
            var start = 0;
            final var end = value.length();
            for (var at = 0; at < end; at++)
            {
                switch (value.charAt(at))
                {
                    case ';':
                    case ':':
                        split.add(value.substring(start, at));
                        start = at + 1;
                        break;
                }
            }
            if (start <= end)
            {
                split.add(value.substring(start, end));
            }
        }
        return split;
    }

    private void capacity(final int capacity)
    {
        if (tags == null && capacity > FIELDS)
        {
            tags = new HashMap<>(32);

            put(key0, value0);
            put(key1, value1);
            put(key2, value2);
            put(key3, value3);
            put(key4, value4);
            put(key5, value5);
            put(key6, value6);
            put(key7, value7);

            key0 = value0 = null;
            key1 = value1 = null;
            key2 = value2 = null;
            key3 = value3 = null;
            key4 = value4 = null;
            key5 = value5 = null;
            key6 = value6 = null;
            key7 = value7 = null;

            size = -1;
        }
    }

    private String compactKey(final int index)
    {
        switch (index)
        {
            case 0:
                return key0;
            case 1:
                return key1;
            case 2:
                return key2;
            case 3:
                return key3;
            case 4:
                return key4;
            case 5:
                return key5;
            case 6:
                return key6;
            case 7:
                return key7;
        }
        ensure(false);
        return null;
    }

    private boolean isCompact()
    {
        return tags == null;
    }

    private void setField(final int index, final String key, final String value)
    {
        switch (index)
        {
            case 0:
                key0 = key;
                value0 = value;
                break;

            case 1:
                key1 = key;
                value1 = value;
                break;

            case 2:
                key2 = key;
                value2 = value;
                break;

            case 3:
                key3 = key;
                value3 = value;
                break;

            case 4:
                key4 = key;
                value4 = value;
                break;

            case 5:
                key5 = key;
                value5 = value;
                break;

            case 6:
                key6 = key;
                value6 = value;
                break;

            case 7:
                key7 = key;
                value7 = value;
                break;
        }
    }
}
