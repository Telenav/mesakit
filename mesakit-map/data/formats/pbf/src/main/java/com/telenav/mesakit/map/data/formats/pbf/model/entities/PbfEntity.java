package com.telenav.mesakit.map.data.formats.pbf.model.entities;

import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelEntities;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Date;
import java.util.Iterator;

import static com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat.PROGRAMMATIC_IDENTIFIER;
import static com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat.USER_LABEL_IDENTIFIER;

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
@UmlExcludeSuperTypes({ AsString.class, Iterable.class })
public abstract class PbfEntity<T extends Entity> implements AsString, Iterable<Tag>
{
    private final T entity;

    private PbfTagMap tagMap;

    private PbfTagList tagList;

    public PbfEntity(final T entity)
    {
        this.entity = entity;
    }

    @Override
    public String asString(final StringFormat format)
    {
        switch (format.identifier())
        {
            case PROGRAMMATIC_IDENTIFIER:
                return Long.toString(identifierAsLong());

            case USER_LABEL_IDENTIFIER:
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

    public boolean hasKey(final String key)
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

    public PbfTagList tagList(final PbfTagFilter filter)
    {
        return PbfTagList.from(entity.getTags(), filter);
    }

    public PbfTagMap tagMap(final PbfTagFilter filter)
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

    public String tagValue(final String key, final String defaultValue)
    {
        final var value = tagMap().get(key);
        return value == null ? defaultValue : value;
    }

    public String tagValue(final String key)
    {
        return tagMap().get(key);
    }

    public Count tagValueAsCount(final String key)
    {
        return Count.parse(tagValue(key));
    }

    public int tagValueAsInt(final String key)
    {
        return tagMap().valueAsInt(key);
    }

    public int tagValueAsNaturalNumber(final String key)
    {
        return tagMap().valueAsNaturalNumber(key);
    }

    public StringList tagValueAsWords(final String key)
    {
        return StringList.words(tagValue(key));
    }

    public boolean tagValueIs(final String key, final String value)
    {
        return value.equals(tagValue(key));
    }

    public boolean tagValueIsNegativeOne(final String key)
    {
        return "-1".equals(tagValue(key));
    }

    public boolean tagValueIsNo(final String key)
    {
        return tagMap().valueIsNo(key);
    }

    public boolean tagValueIsYes(final String key)
    {
        return tagMap().valueIsYes(key);
    }

    public StringList tagValueSplit(final String key, final char delimiter)
    {
        return StringList.split(tagValue(key), delimiter);
    }

    public StringList tagValueSplit(final String key)
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
        return asString(StringFormat.USER_LABEL);
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
