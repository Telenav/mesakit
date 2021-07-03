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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelTags;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@UmlClassDiagram(diagram = DiagramPbfModelTags.class)
public class PbfTagPatternFilter implements PbfTagFilter
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public static PbfTagPatternFilter forPattern(final String pattern)
    {
        return new PbfTagPatternFilter(pattern);
    }

    public static SwitchParser.Builder<PbfTagPatternFilter> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(PbfTagPatternFilter.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER));
    }

    public static SwitchParser.Builder<PbfTagPatternFilter> tagFilterSwitchParser()
    {
        return switchParser("tag-filter", "The regular expression used to filter tags by key");
    }

    public static class Converter extends BaseStringConverter<PbfTagPatternFilter>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected PbfTagPatternFilter onToValue(final String value)
        {
            return forPattern(value);
        }
    }

    private final Pattern pattern;

    private final Map<String, Boolean> filteredTags = new HashMap<>();

    public PbfTagPatternFilter(final String pattern)
    {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean accepts(final Tag tag)
    {
        var accepted = filteredTags.get(tag.getKey());
        if (accepted == null)
        {
            final var matcher = pattern.matcher(tag.getKey());
            accepted = matcher.matches();
            filteredTags.put(tag.getKey(), accepted);
            if (!accepted)
            {
                DEBUG.trace("osm tag ${debug} is rejected.", tag.getKey());
            }
        }
        return accepted;
    }

    public Set<String> allFilteredTags()
    {
        return filteredTags.keySet();
    }

    public Set<String> rejectedTags()
    {
        final Set<String> rejectedTags = new HashSet<>();
        for (final var entry : filteredTags.entrySet())
        {
            if (!entry.getValue())
            {
                rejectedTags.add(entry.getKey());
            }
        }
        return rejectedTags;
    }
}
