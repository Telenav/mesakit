package com.telenav.mesakit.map.data.formats.pbf.model.entities;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelEntities;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Date;
import java.util.Iterator;

/**
 * This wrapper (and {@link PbfNode}, {@link PbfWay} and {@link PbfRelation} subclasses) adds a slight amount of
 * overhead, but accessing tags and especially constructing tag maps is such a hotspot that it makes sense to add this
 * wrapper. The major benefit is that the {@link PbfTagMap} associated with an entity is cached along with the object,
 * which ensures that different use points have access to a single tag map for the entity without the need to construct
 * a map that has potentially already been constructed. In addition to this benefit, this class and its subclasses
 * provide convenient methods for working with entities.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelEntities.class)
@UmlExcludeSuperTypes({ Stringable.class, Iterable.class })
public abstract class PbfEntity<T extends Entity> implements Stringable, Iterable<Tag>
{
    private final T entity;

    private PbfTagList tagList;

    private PbfTagMap tagMap;

    public PbfEntity(T entity)
    {
        this.entity = entity;
    }

    @Override
    public String asString(Format format)
    {
        switch (format)
        {
            case PROGRAMMATIC:
                return Long.toString(identifierAsLong());

            case USER_LABEL:
            default:
                return "[" + type() + " id = " + entity.getId() + ", tags = " + tagList() + "]";
        }
    }

    public long changeSetIdentifier()
    {
        return entity.getChangesetId();
    }

    public T get()
    {
        return entity;
    }

    public boolean hasKey(String key)
    {
        return tagMap().containsKey(key);
    }

    public boolean hasTags()
    {
        return tagCount() > 0;
    }

    public abstract MapIdentifier identifier();

    public long identifierAsLong()
    {
        return entity.getId();
    }

    @NotNull
    @Override
    public Iterator<Tag> iterator()
    {
        return entity.getTags().iterator();
    }

    public int tagCount()
    {
        return entity.getTags().size();
    }

    public PbfTagList tagList()
    {
        if (tagList == null)
        {
            tagList = PbfTagList.from(entity.getTags());
        }
        return tagList;
    }

    public PbfTagList tagList(PbfTagFilter filter)
    {
        return PbfTagList.from(entity.getTags(), filter);
    }

    public PbfTagMap tagMap(PbfTagFilter filter)
    {
        return PbfTagMap.from(entity.getTags(), filter);
    }

    public PbfTagMap tagMap()
    {
        if (tagMap == null)
        {
            tagMap = PbfTagMap.from(entity.getTags());
        }
        return tagMap;
    }

    public String tagValue(String key, String defaultValue)
    {
        var value = tagMap().get(key);
        return value == null ? defaultValue : value;
    }

    public String tagValue(String key)
    {
        return tagMap().get(key);
    }

    public Count tagValueAsCount(String key)
    {
        return Count.parseCount(Listener.consoleListener(), tagValue(key));
    }

    public int tagValueAsInt(String key)
    {
        return tagMap().valueAsInt(key);
    }

    public int tagValueAsNaturalNumber(String key)
    {
        return tagMap().valueAsNaturalNumber(key);
    }

    public StringList tagValueAsWords(String key)
    {
        return StringList.words(tagValue(key));
    }

    public boolean tagValueIs(String key, String value)
    {
        return value.equals(tagValue(key));
    }

    public boolean tagValueIsNegativeOne(String key)
    {
        return "-1".equals(tagValue(key));
    }

    public boolean tagValueIsNo(String key)
    {
        return tagMap().valueIsNo(key);
    }

    public boolean tagValueIsYes(String key)
    {
        return tagMap().valueIsYes(key);
    }

    public StringList tagValueSplit(String key, char delimiter)
    {
        return StringList.split(tagValue(key), delimiter);
    }

    public StringList tagValueSplit(String key)
    {
        return tagMap().valueSplit(key);
    }

    public Date timestamp()
    {
        return entity.getTimestamp();
    }

    @Override
    public String toString()
    {
        return asString(Format.USER_LABEL);
    }

    public EntityType type()
    {
        return entity.getType();
    }

    public OsmUser user()
    {
        return entity.getUser();
    }

    public int version()
    {
        return entity.getVersion();
    }
}
