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

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.map.StringMap;
import com.telenav.kivakit.interfaces.collection.Keyed;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelTags;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static com.telenav.kivakit.core.language.primitive.Ints.parseFastNaturalNumber;
import static com.telenav.kivakit.core.language.primitive.Ints.parseInt;
import static com.telenav.kivakit.core.language.primitive.Longs.parseFastLong;
import static com.telenav.kivakit.core.messaging.Listener.consoleListener;

/**
 * A map between tag keys and values.
 * <p>
 * HOTSPOT: This class has been identified as a hotpot by YourKit and so it has been optimized to avoid allocating
 * {@link HashMap} instances for smaller tag maps. The average number of tags per entity in OSM is between 2.3 and 3.95,
 * so 8 fields should cover the majority of OSM tag maps.
 */
@SuppressWarnings({ "StaticInitializerReferencesSubClass", "DuplicatedCode" })
@UmlClassDiagram(diagram = DiagramPbfModelTags.class)
@UmlRelation(label = "stores", referent = Tag.class)
@UmlExcludeSuperTypes(Iterable.class)
public class PbfTagMap implements Iterable<Tag>, Keyed<String, String>
{
    /** Empty tag map */
    public static final PbfTagMap EMPTY = new EmptyPbfTagMap();

    public static PbfTagMap create()
    {
        return new PbfTagMap(1);
    }

    public static PbfTagMap from(Collection<Tag> tags)
    {
        if (tags.isEmpty())
        {
            return EMPTY;
        }
        var map = new PbfTagMap(tags.size());
        for (var tag : tags)
        {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }

    public static PbfTagMap from(Collection<Tag> tags, PbfTagFilter filter)
    {
        if (tags.isEmpty())
        {
            return EMPTY;
        }
        var map = new PbfTagMap(tags.size());
        for (var tag : tags)
        {
            if (filter.accepts(tag))
            {
                map.put(tag.getKey(), tag.getValue());
            }
        }
        return map;
    }

    // Full hashmap when there are more than FIELDS tags
    private final StringMap<String> tags;

    protected PbfTagMap(int initialCapacity)
    {
        tags = new StringMap<>();
    }

    public boolean containsKey(String key)
    {
        return tags.containsKey(key);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof PbfTagMap that)
        {
            return tags.equals(that.tags);
        }
        return false;
    }

    @Override
    public String get(String key)
    {
        return tags.get(key);
    }

    public String get(String key, String defaultValue)
    {
        return tags.getOrDefault(key, defaultValue);
    }

    @Override
    public int hashCode()
    {
        return tags.hashCode();
    }

    public boolean isEmpty()
    {
        return tags.isEmpty();
    }

    @Override
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
                var at = keys.next();
                return new Tag(at, get(at));
            }
        };
    }

    public Iterator<String> keys()
    {
        return tags.keySet().iterator();
    }

    public void put(String key, String value)
    {
        tags.put(key, value);
    }

    public void putAll(Iterable<Tag> tags)
    {
        for (var tag : tags)
        {
            put(tag.getKey(), tag.getValue());
        }
    }

    public int size()
    {
        return tags.size();
    }

    public Tag tag(String key)
    {
        return new Tag(key, get(key));
    }

    public String value(String key)
    {
        return get(key);
    }

    public int valueAsInt(String key)
    {
        return parseInt(consoleListener(), get(key));
    }

    public long valueAsLong(String key)
    {
        return parseFastLong(get(key));
    }

    public int valueAsNaturalNumber(String key)
    {
        return parseFastNaturalNumber(get(key));
    }

    public boolean valueIsNo(String key)
    {
        var value = value(key);
        if (value != null)
        {
            return "no".equals(value);
        }
        return false;
    }

    public boolean valueIsYes(String key)
    {
        var value = value(key);
        if (value != null)
        {
            return "yes".equals(value);
        }
        return false;
    }

    public StringList valueSplit(String key)
    {
        var split = new StringList();
        var value = get(key);
        if (value != null)
        {
            var start = 0;
            var end = value.length();
            for (var at = 0; at < end; at++)
            {
                switch (value.charAt(at))
                {
                    case ';', ':' ->
                    {
                        split.add(value.substring(start, at));
                        start = at + 1;
                    }
                }
            }
            if (start <= end)
            {
                split.add(value.substring(start, end));
            }
        }
        return split;
    }
}
