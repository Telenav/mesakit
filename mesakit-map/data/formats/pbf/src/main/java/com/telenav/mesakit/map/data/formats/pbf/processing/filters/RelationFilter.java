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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.map.StringMap;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.interfaces.comparison.Filter;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfProcessingFilters;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.core.collections.map.StringMap.KeyCaseSensitivity.FOLD_CASE_LOWER;
import static com.telenav.kivakit.core.ensure.Ensure.fail;

@SuppressWarnings("DuplicatedCode") @UmlClassDiagram(diagram = DiagramPbfProcessingFilters.class)
@UmlExcludeSuperTypes(Named.class)
public class RelationFilter implements Filter<PbfRelation>, Named
{
    private static final StringMap<RelationFilter> relationFilterForName = new StringMap<>();

    public static RelationFilter forName(String name)
    {
        var filter = relationFilterForName.get(name);
        if (filter != null)
        {
            return filter;
        }
        return fail("Unrecognized relation filter '" + name + "'");
    }

    public static SwitchParser.Builder<RelationFilter> relationFilterSwitchParser(Listener listener)
    {
        PbfFilters.loadAll();
        return relationFilterSwitchParser(listener, "relation-filter", "The name of a relation filter:\n\n" + help() + "\n");
    }

    public static SwitchParser.Builder<RelationFilter> relationFilterSwitchParser(Listener listener, String name,
                                                                                  String description)
    {
        return SwitchParser.switchParser(RelationFilter.class).name(name).description(description)
                .converter(new Converter(listener));
    }

    public static class Converter extends BaseStringConverter<RelationFilter>
    {
        public Converter(Listener listener)
        {
            super(listener, RelationFilter.class);
        }

        @Override
        protected RelationFilter onToValue(String value)
        {
            return forName(value);
        }
    }

    private final String description;

    private final Set<String> excluded = new HashSet<>();

    private final Set<String> included = new HashSet<>();

    private final String name;

    public RelationFilter(String name, String description)
    {
        this.name = name;
        this.description = description;
        relationFilterForName.put(name, this);
    }

    @Override
    public boolean accepts(PbfRelation relation)
    {
        // Ensure if we're including or excluding
        var include = !included.isEmpty();
        var exclude = !excluded.isEmpty();

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

    public void exclude(String type)
    {
        excluded.add(type);
    }

    public void include(String type)
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

    protected boolean isExcluded(PbfRelation relation)
    {
        if (relation.hasKey("type"))
        {
            var type = relation.tagValue("type");
            return excluded.contains(type);
        }
        return false;
    }

    protected boolean isIncluded(PbfRelation relation)
    {
        if (relation.hasKey("type"))
        {
            var type = relation.tagValue("type");
            return included.contains(type);
        }
        return false;
    }

    private static String help()
    {
        var help = new StringList();
        for (var name : relationFilterForName.keySet())
        {
            var filter = relationFilterForName.get(name);
            help.add(name + " - " + filter.description());
        }
        help.sort(Comparator.naturalOrder());
        return help.bulleted(4);
    }
}
