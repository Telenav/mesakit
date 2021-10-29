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
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.interfaces.comparison.Filter;
import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.collections.map.string.NameMap;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessingFilters;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

@UmlClassDiagram(diagram = DiagramPbfProcessingFilters.class)
@UmlExcludeSuperTypes(Named.class)
public class WayFilter implements Filter<PbfWay>, Named
{
    private static final NameMap<WayFilter> wayFilterForName = new NameMap<>();

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static WayFilter exclude(String name, Resource resource)
    {
        var filter = new WayFilter(name, "exclude list from resource");
        for (var line : resource.reader().lines(ProgressReporter.NULL))
        {
            var highway = line.trim();
            if (!Strings.isEmpty(highway))
            {
                filter.exclude(highway);
            }
        }
        return filter;
    }

    public static WayFilter forName(String name)
    {
        var filter = wayFilterForName.get(name);
        if (filter != null)
        {
            return filter;
        }
        return fail("Unrecognized way filter '" + name + "'");
    }

    public static WayFilter include(String name, Resource resource)
    {
        var filter = new WayFilter(name, "include list from resource");
        for (var line : resource.reader().lines(ProgressReporter.NULL))
        {
            var highway = line.trim();
            if (!Strings.isEmpty(highway))
            {
                filter.include(highway);
            }
        }
        return filter;
    }

    public static SwitchParser.Builder<WayFilter> wayFilterSwitchParser(String name, String description)
    {
        return SwitchParser.builder(WayFilter.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER));
    }

    public static SwitchParser.Builder<WayFilter> wayFilterSwitchParser()
    {
        PbfFilters.loadAll();
        return wayFilterSwitchParser("way-filter", "The name of a way filter:\n\n" + help());
    }

    public static class Converter extends BaseStringConverter<WayFilter>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected WayFilter onToValue(String value)
        {
            return forName(value);
        }
    }

    private final Set<String> excluded = new HashSet<>();

    private final Set<String> included = new HashSet<>();

    private final String name;

    private final String description;

    public WayFilter(String name, String description)
    {
        this.name = name;
        this.description = description;
        wayFilterForName.add(this);
    }

    @Override
    public boolean accepts(PbfWay way)
    {
        // We don't accept anything that's not a highway or ferry route at this time
        if (!(isHighway(way) || isFerryRoute(way)))
        {
            return false;
        }

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
            // return true if this way is included
            return isIncluded(way);
        }

        // If we are an exclude filter
        if (exclude)
        {
            // return true if this way is included
            return !isExcluded(way);
        }

        fail("Nothing included or excluded");
        return false;
    }

    public String description()
    {
        return description;
    }

    public void exclude(String highway)
    {
        excluded.add(highway);
    }

    public void include(String highway)
    {
        included.add(highway);
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

    protected boolean isExcluded(PbfWay way)
    {
        for (var highway : way.highways())
        {
            if (excluded.contains(highway))
            {
                return true;
            }
        }
        if (way.tagValueIsYes("area"))
        {
            return true;
        }
        return "ferry".equals(way.tagValue("route")) && excluded.contains("ferry");
    }

    protected boolean isIncluded(PbfWay way)
    {
        for (var highway : way.highways())
        {
            if (included.contains(highway))
            {
                return true;
            }
        }
        return "ferry".equals(way.tagValue("route")) && included.contains("ferry");
    }

    private static String help()
    {
        var help = new StringList();
        for (var name : wayFilterForName.keySet())
        {
            var filter = wayFilterForName.get(name);
            help.add(name + " - " + filter.description());
        }
        help.sort(Comparator.naturalOrder());
        return help.bulleted(4) + "\n";
    }

    private boolean isFerryRoute(PbfWay way)
    {
        return way.hasKey("route") && "ferry".equals(way.tagValue("route"));
    }

    private boolean isHighway(PbfWay way)
    {
        return way.hasKey("highway");
    }
}
