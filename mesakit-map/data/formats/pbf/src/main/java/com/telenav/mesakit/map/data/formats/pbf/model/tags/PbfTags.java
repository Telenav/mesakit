package com.telenav.mesakit.map.data.formats.pbf.model.tags;

import com.telenav.kivakit.core.string.Strip;
import com.telenav.kivakit.resource.resources.other.PropertyMap;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.DiagramPbfModelTags;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelTags.class)
@UmlRelation(label = "provides utilities for", referent = Tag.class)
public class PbfTags
{
    private static final Pattern TAG = Pattern.compile("(?<key>[A-Za-z0-9:-_*]+)"
            + "\\s*=\\s*"
            + "(?<value>((?<quote>[\"']).*?\\k<quote>)|([^ ]+))");

    public static boolean equals(Tag a, Tag b)
    {
        if (a == null)
        {
            return b == null;
        }
        return a.getKey().equals(b.getKey()) && a.getValue().equals(b.getValue());
    }

    /**
     * Parses tags of the forms:
     * <ul>
     *     <li>tag=value</li>
     *     <li>tag="value with spaces"</li>
     * </ul>
     */
    public static Tag parse(String string)
    {
        var matcher = TAG.matcher(string);
        if (matcher.matches())
        {
            var key = matcher.group("key");
            var value = matcher.group("value");
            return new Tag(key, Strip.quotes(value));
        }
        return null;
    }

    public static PropertyMap tags(String prefix, Collection<Tag> tags)
    {
        var properties = PropertyMap.create();
        for (var tag : tags)
        {
            var key = tag.getKey();
            if (key.startsWith(prefix))
            {
                properties.put(key.substring(prefix.length()), tag.getValue());
            }
        }
        return properties;
    }

    public static List<Tag> tags(String prefix, PropertyMap properties)
    {
        var tags = new ArrayList<Tag>();
        for (var property : properties.entrySet())
        {
            tags.add(new Tag(prefix + property.getKey(), property.getValue()));
        }
        return tags;
    }
}
