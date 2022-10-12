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

package com.telenav.mesakit.graph.library.osm.change;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;

import static com.telenav.kivakit.core.string.Strings.isNullOrEmpty;

/**
 * Mutable way is the superclass of {@link MutableWay} and {@link NewWay} since both modifiable ways in the base may and
 * new ways are both mutable and need to be treated the same with regards to tag modifications.
 *
 * @author jonathanl (shibo)
 */
public abstract class BaseMutableWay
{
    /**
     * Adds any name tag from the enhancing way to the base way so long as the base way isn't a link or roundabout and
     * doesn't already have a "ref" or "name" tag.
     */
    public String addNameTag(Edge base, Edge enhancing)
    {
        // If the enhancing edge has a name
        if (!enhancing.isNameless())
        {
            // and the base edge is not an osm link or roundabout and it doesn't have a "ref" or
            // "name" tag already
            if (!base.osmIsLink() && !base.osmIsRoundabout() && !base.hasTag("ref") && !base.hasTag("name"))
            {
                // add the enhancing edge's name to the base edge
                var name = normalizeName(enhancing.tagValue("name"));
                addTag("name", name);
                return name;
            }
        }
        return null;
    }

    public abstract void addNewTags(PbfTagList tags);

    /**
     * Adds any one way tag from the enhancing way if the the base way doesn't already have a "oneway" or "junction"
     * tag.
     */
    public String addOneWayTag(Edge base, Edge enhancing)
    {
        // If the enhancing edge is one way and the base does not already have a "oneway"
        // tag or a "junction" tag,
        if (enhancing.osmIsOneWay() && !base.hasTag("oneway") && !base.hasTag("junction"))
        {
            // then add the "oneway" tag from the enhancing edge
            var oneway = enhancing.tagValue("oneway");
            addTag("oneway", oneway);
            return oneway;
        }
        return null;
    }

    /**
     * Add the given tag from the enhancing edge to the base edge if the enhancing tag is non-null and the base edge
     * doesn't already have a value for the tag.
     */
    public String addTag(Edge base, Edge enhancing, String tag)
    {
        // If the tag value is non-empty and the base doesn't have a value for the tag
        var value = enhancing.tagValue(tag);
        if (!isNullOrEmpty(value) && !base.hasTag(tag))
        {
            // then add the enhancing tag
            addTag(tag, value);
            return value;
        }
        return null;
    }

    public abstract void addTag(String key, String value);

    public abstract void changeTag(String key, String baseValue, String enhancingValue);

    private String normalizeName(String value)
    {
        if (value != null)
        {
            return value.replace('-', ' ');
        }
        return null;
    }
}
