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

import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.string.AsIndentedString;
import com.telenav.kivakit.core.string.AsStringIndenter;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelTags;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

@SuppressWarnings("DuplicatedCode") @UmlClassDiagram(diagram = DiagramPbfModelTags.class)
@UmlExcludeSuperTypes({ AsIndentedString.class, Iterable.class })
@UmlRelation(label = "stores", referent = Tag.class)
public class PbfTagList implements Iterable<Tag>, AsIndentedString
{
    public static final PbfTagList EMPTY = new PbfTagList(0);

    private static final int FIELDS = 8;

    private static final int NO_SIZE = -1;

    public static PbfTagList create()
    {
        return new PbfTagList(FIELDS);
    }

    public static PbfTagList from(Collection<Tag> tags)
    {
        var list = new PbfTagList(tags.size());
        list.addAll(tags);
        return list;
    }

    public static PbfTagList from(Collection<Tag> tags, PbfTagFilter filter)
    {
        var list = new PbfTagList(tags.size());
        for (var tag : tags)
        {
            if (filter.accepts(tag))
            {
                list.add(tag.getKey(), tag.getValue());
            }
        }
        return list;
    }

    public static PbfTagList of(Tag tag)
    {
        var list = new PbfTagList(1);
        list.add(tag);
        return list;
    }

    // Simple fields when the size <= FIELDS
    private String key0, value0;

    private String key1, value1;

    private String key2, value2;

    private String key3, value3;

    private String key4, value4;

    private String key5, value5;

    private String key6, value6;

    private String key7, value7;

    // The size of this list
    private int size;

    // Full array list when there are more than FIELDS tags
    private List<Tag> tags;

    private PbfTagList(int capacity)
    {
        if (capacity > FIELDS)
        {
            tags = new ArrayList<>(capacity);
        }
    }

    public PbfTagList add(Tag tag)
    {
        if (isCompact())
        {
            add(tag.getKey(), tag.getValue());
        }
        else
        {
            if (tag != null)
            {
                tags.add(tag);
            }
        }
        return this;
    }

    public PbfTagList add(String key, String value)
    {
        assert key != null;
        assert value != null;

        set(size(), key, value);

        return this;
    }

    public PbfTagList addAll(Iterable<Tag> tags)
    {
        for (var tag : tags)
        {
            add(tag);
        }
        return this;
    }

    /**
     * @return A proper list of tags, possibly converted from fields
     */
    public List<Tag> asList()
    {
        // Force allocation of list if not already allocated
        size(Math.max(16, size()));
        return tags;
    }

    @Override
    public AsStringIndenter asString(StringFormattable.Format format, AsStringIndenter indenter)
    {
        indenter.bracketed(sorted(), tag -> indenter.add(tag.toString()));
        return indenter;
    }

    public boolean containsKey(String key)
    {
        for (var index = 0; index < size(); index++)
        {
            if (key.equals(key(index)))
            {
                return true;
            }
        }
        return false;
    }

    public PbfTagList copy()
    {
        var copy = new PbfTagList(size());
        copy.addAll(this);
        return copy;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof PbfTagList)
        {
            var that = (PbfTagList) object;
            var size = size();
            if (size == that.size())
            {
                for (var i = 0; i < size; i++)
                {
                    if (!PbfTags.equals(get(i), that.get(i)))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Tag get(int index)
    {
        if (isCompact())
        {
            switch (index)
            {
                case 0:
                    return new Tag(key0, value0);

                case 1:
                    return new Tag(key1, value1);

                case 2:
                    return new Tag(key2, value2);

                case 3:
                    return new Tag(key3, value3);

                case 4:
                    return new Tag(key4, value4);

                case 5:
                    return new Tag(key5, value5);

                case 6:
                    return new Tag(key6, value6);

                case 7:
                    return new Tag(key7, value7);

                default:
                    return null;
            }
        }
        else
        {
            return tags.get(index);
        }
    }

    public Tag get(String key)
    {
        if (isCompact())
        {
            var value = valueForKey(key);
            if (value != null)
            {
                return new Tag(key, value);
            }
        }
        else
        {
            for (var i = 0; i < size(); i++)
            {
                if (key.equals(key(i)))
                {
                    return new Tag(key, value(i));
                }
            }
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(key0, value0,
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

    public boolean isValid()
    {
        for (var tag : this)
        {
            if (tag.getKey() == null || tag.getValue() == null)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Tag> iterator()
    {
        return new Iterator<>()
        {
            int at;

            @Override
            public boolean hasNext()
            {
                return at < size();
            }

            @Override
            public Tag next()
            {
                return get(at++);
            }
        };
    }

    public PbfTagList matchingKey(String key)
    {
        var copy = new PbfTagList(size());
        for (var tag : this)
        {
            if (tag.getKey().equals(key))
            {
                copy.add(tag);
            }
        }
        return copy;
    }

    public void removeKey(String key)
    {
        asList().removeIf(next -> next.getKey().equalsIgnoreCase(key));
    }

    public void retainOnly(Set<String> keys)
    {
        asList().removeIf(tag -> !keys.contains(tag.getKey()));
    }

    /**
     * Sets the key and/or value of the tag at the given index.
     * <p>
     * If either the key or the value is null, that aspect of the tag at the given index remains unchanged
     */
    public PbfTagList set(int index, String key, String value)
    {
        // Increase capacity by one, if necessary,
        if (index + 1 > size())
        {
            size(index + 1);
        }

        // and if we're still compact,
        if (isCompact())
        {
            // and we have a key,
            if (key != null)
            {
                // store it in the right field
                fieldKey(index, key);
            }

            // and if we have a value
            if (value != null)
            {
                // store that in the right field
                fieldValue(index, value);
            }
        }
        else
        {
            if (index >= tags.size())
            {
                for (var i = size(); i <= index; i++)
                {
                    tags.add(null);
                }
            }
            var tag = tags.get(index);
            if (tag == null)
            {
                tag = new Tag(key, value);
            }
            else
            {
                tag = new Tag(key != null ? key : tag.getKey(), value != null ? value : tag.getValue());
            }
            tags.set(index, tag);
        }

        return this;
    }

    public int size()
    {
        return tags == null ? size : tags.size();
    }

    /**
     * @return This tag list sorted alphabetically by key
     */
    public PbfTagList sorted()
    {
        var copy = copy();
        copy.asList();
        copy.tags.sort(Comparator.comparing(Tag::getKey));
        return copy;
    }

    @Override
    public String toString()
    {
        List<Tag> list;
        if (isCompact())
        {
            list = new ArrayList<>();
            for (var index = 0; index < size(); index++)
            {
                list.add(get(index));
            }
        }
        else
        {
            asList();
            list = tags;
        }
        return "[PbfTagList tags = " + list + "]";
    }

    /**
     * The value for the given key, or null if the key is not in this tag list
     */
    public String valueForKey(String key)
    {
        if (isCompact())
        {
            for (var index = 0; index < size(); index++)
            {
                if (key.equals(fieldKey(index)))
                {
                    return fieldValue(index);
                }
            }
        }
        else
        {
            var tag = get(key);
            if (tag != null)
            {
                return tag.getValue();
            }
        }
        return null;
    }

    public String valueForKey(String key, String defaultValue)
    {
        var value = valueForKey(key);
        if (value == null)
        {
            return defaultValue;
        }
        return value;
    }

    public PbfTagList withoutKey(String key)
    {
        var copy = new PbfTagList(size());
        for (var tag : this)
        {
            if (!tag.getKey().equals(key))
            {
                copy.add(tag);
            }
        }
        return copy;
    }

    private String fieldKey(int index)
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
        return fail("Invalid field index");
    }

    private void fieldKey(int index, String key)
    {
        switch (index)
        {
            case 0:
                key0 = key;
                break;

            case 1:
                key1 = key;
                break;

            case 2:
                key2 = key;
                break;

            case 3:
                key3 = key;
                break;

            case 4:
                key4 = key;
                break;

            case 5:
                key5 = key;
                break;

            case 6:
                key6 = key;
                break;

            case 7:
                key7 = key;
                break;
        }
    }

    private String fieldValue(int index)
    {
        switch (index)
        {
            case 0:
                return value0;

            case 1:
                return value1;

            case 2:
                return value2;

            case 3:
                return value3;

            case 4:
                return value4;

            case 5:
                return value5;

            case 6:
                return value6;

            case 7:
                return value7;
        }
        return fail("Invalid field index");
    }

    private void fieldValue(int index, String value)
    {
        switch (index)
        {
            case 0:
                value0 = value;
                break;

            case 1:
                value1 = value;
                break;

            case 2:
                value2 = value;
                break;

            case 3:
                value3 = value;
                break;

            case 4:
                value4 = value;
                break;

            case 5:
                value5 = value;
                break;

            case 6:
                value6 = value;
                break;

            case 7:
                value7 = value;
                break;

            default:
                fail("Invalid field index");
        }
    }

    /**
     * @return True if fields are being used to hold key / value pairs
     */
    private boolean isCompact()
    {
        return tags == null;
    }

    private String key(int index)
    {
        if (isCompact())
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

                default:
                    return null;
            }
        }
        else
        {
            var tag = tags.get(index);
            return tag == null ? null : tag.getKey();
        }
    }

    private void size(int size)
    {
        if (tags == null && size > FIELDS)
        {
            var tags = new ArrayList<Tag>(16);
            for (var index = 0; index < this.size; index++)
            {
                tags.add(new Tag(key(index), value(index)));
            }
            this.tags = tags;

            key0 = null;
            value0 = null;
            key1 = null;
            value1 = null;
            key2 = null;
            value2 = null;
            key3 = null;
            value3 = null;
            key4 = null;
            value4 = null;
            key5 = null;
            value5 = null;
            key6 = null;
            value6 = null;
            key7 = null;
            value7 = null;

            this.size = NO_SIZE;
        }
        else
        {
            this.size = size;
        }
    }

    private String value(int index)
    {
        if (isCompact())
        {
            switch (index)
            {
                case 0:
                    return value0;

                case 1:
                    return value1;

                case 2:
                    return value2;

                case 3:
                    return value3;

                case 4:
                    return value4;

                case 5:
                    return value5;

                case 6:
                    return value6;

                case 7:
                    return value7;

                default:
                    return null;
            }
        }
        else
        {
            return tags.get(index).getValue();
        }
    }
}
