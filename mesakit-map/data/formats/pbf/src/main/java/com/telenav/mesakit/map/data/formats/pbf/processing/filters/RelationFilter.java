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

package com.telenav.mesakit.map.data.formats.pbf.processing.filters;

import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessingFilters;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.interfaces.comparison.Filter;
import com.telenav.kivakit.core.kernel.interfaces.naming.Named;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.collections.map.string.NameMap;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Listener;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

@UmlClassDiagram(diagram = DiagramPbfProcessingFilters.class)
@UmlExcludeSuperTypes(Named.class)
public class RelationFilter implements Filter<PbfRelation>, Named
{
    private static final NameMap<RelationFilter> relationFilterForName = new NameMap<>();

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static RelationFilter forName(final String name)
    {
        final var filter = relationFilterForName.get(name);
        if (filter != null)
        {
            return filter;
        }
        return fail("Unrecognized relation filter '" + name + "'");
    }

    public static SwitchParser.Builder<RelationFilter> relationFilter()
    {
        PbfFilters.loadAll();
        return switchParser("relation-filter", "The name of a relation filter:\n\n" + help() + "\n");
    }

    public static SwitchParser.Builder<RelationFilter> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(RelationFilter.class).name(name).description(description)
                .converter(new Converter(LOGGER));
    }

    public static class Converter extends BaseStringConverter<RelationFilter>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected RelationFilter onConvertToObject(final String value)
        {
            return forName(value);
        }
    }

    private final String name;

    private final String description;

    private final Set<String> excluded = new HashSet<>();

    private final Set<String> included = new HashSet<>();

    public RelationFilter(final String name, final String description)
    {
        this.name = name;
        this.description = description;
        relationFilterForName.add(this);
    }

    @Override
    public boolean accepts(final PbfRelation relation)
    {
        // Ensure if we're including or excluding
        final var include = !included.isEmpty();
        final var exclude = !excluded.isEmpty();

        // Don't allow both options
        if (exclude && include)
        {
            fail("Cannot include and exclude at the same time");
            return false;
        }

        // If we are an include filter
        if (include)
        {
            // return true if this relation is included
            return isIncluded(relation);
        }

        // If we are an exclude filter
        if (exclude)
        {
            // return true if this relation is included
            return !isExcluded(relation);
        }

        fail("Nothing included or excluded");
        return false;
    }

    public String description()
    {
        return description;
    }

    public void exclude(final String type)
    {
        excluded.add(type);
    }

    public void include(final String type)
    {
        included.add(type);
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public final String toString()
    {
        return name();
    }

    protected boolean isExcluded(final PbfRelation relation)
    {
        if (relation.hasKey("type"))
        {
            final var type = relation.tagValue("type");
            return excluded.contains(type);
        }
        return false;
    }

    protected boolean isIncluded(final PbfRelation relation)
    {
        if (relation.hasKey("type"))
        {
            final var type = relation.tagValue("type");
            return included.contains(type);
        }
        return false;
    }

    private static String help()
    {
        final var help = new StringList();
        for (final var name : relationFilterForName.keySet())
        {
            final var filter = relationFilterForName.get(name);
            help.add(name + " - " + filter.description());
        }
        help.sort(Comparator.naturalOrder());
        return help.bulleted(4);
    }
}
