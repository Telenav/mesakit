package com.telenav.mesakit.map.data.formats.pbf.model.tags;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.iteration.Iterators;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfModelTags;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Iterator;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelTags.class)
public class EmptyPbfTagMap extends PbfTagMap
{
    protected EmptyPbfTagMap()
    {
        super(0);
    }

    @Override
    public boolean containsKey(String key)
    {
        return false;
    }

    @Override
    public boolean equals(Object object)
    {
        return super.equals(object);
    }

    @Override
    public String get(String key)
    {
        return null;
    }

    @Override
    public String get(String key, String defaultValue)
    {
        return null;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public @NotNull
    Iterator<Tag> iterator()
    {
        return Iterators.empty();
    }

    @Override
    public Iterator<String> keys()
    {
        return Iterators.empty();
    }

    @Override
    public void put(String key, String value)
    {
        unsupported();
    }

    @Override
    public void putAll(Iterable<Tag> tags)
    {
        unsupported();
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public Tag tag(String key)
    {
        return null;
    }

    @Override
    public String value(String key)
    {
        return null;
    }

    @Override
    public int valueAsInt(String key)
    {
        return Ints.INVALID;
    }

    @Override
    public int valueAsNaturalNumber(String key)
    {
        return Ints.INVALID;
    }

    @Override
    public StringList valueSplit(String key)
    {
        return new StringList();
    }
}
