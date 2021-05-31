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

package com.telenav.kivakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.language.string.Strings;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.utilities.locale.Language;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfWay;
import com.telenav.kivakit.map.region.Country;
import com.telenav.kivakit.map.region.locale.MapLocale;
import com.telenav.kivakit.map.road.model.RoadName;
import com.telenav.kivakit.map.road.name.standardizer.RoadNameStandardizer;

import java.util.List;

public abstract class BaseRoadNameExtractor extends BaseExtractor<List<RoadName>, PbfWay>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private RoadNameStandardizer standardizer;

    private final MapLocale locale;

    protected BaseRoadNameExtractor(final MapLocale locale, final RoadNameStandardizer.Mode mode,
                                    final Listener<Message> listener)
    {
        super(listener);
        this.locale = locale;

        if (mode != RoadNameStandardizer.Mode.NO_STANDARDIZATION)
        {
            standardizer = RoadNameStandardizer.get(locale, mode);
        }
    }

    protected void addRoadName(final List<RoadName> names, final String name)
    {
        if (name != null && !name.isBlank())
        {
            if (name.contains(";"))
            {
                for (final var subName : Strings.split(name, ';'))
                {
                    addRoadName(names, subName);
                }
            }
            else
            {
                var roadName = RoadName.forName(name);
                if (roadName != null)
                {
                    if (standardizer != null && shouldStandardize(name))
                    {
                        final var standardized = standardizer.standardize(roadName);
                        if (standardized != null)
                        {
                            roadName = standardized.asRoadName();
                            if (roadName == null)
                            {
                                roadName = RoadName.forName(name);
                                DEBUG.trace("Unable to standardize road name '$'", name);
                            }
                        }
                    }
                    if (!names.contains(roadName))
                    {
                        names.add(roadName);
                    }
                }
            }
        }
    }

    protected void addRoadNameTranslation(final List<RoadName> names, final PbfWay way, final String key)
    {
        final var languages = languages(way);
        for (final var language : languages)
        {
            addRoadName(names, way.tagValue(key + ":" + language.getIso3Code()));
            for (final var translation : languages)
            {
                if (translation != language)
                {
                    addRoadName(names, way.tagValue(key + ":" + language.getIso3Code() + ":trans:" + translation.getIso3Code()));
                }
            }
        }
    }

    protected void addRoadNameTranslations(final List<RoadName> names, final PbfWay way, final String[] keys)
    {
        for (final var key : keys)
        {
            addRoadNameTranslation(names, way, key);
        }
    }

    protected List<Language> languages(final PbfWay way)
    {
        // Get any ISO code from the way
        final var iso = way.tagValue("iso");
        if (iso != null)
        {
            // then get the country for the iso code,
            final var country = Country.forIsoCode(iso);
            if (country != null)
            {
                // and return its languages
                return country.languages();
            }
        }

        // otherwise return the default language for the locale
        return List.of(locale.language());
    }

    /**
     * The underlying road standardizer has problems with some specific cases. It turns a route reference like "17" into
     * "17th" and it turns names with hyphens like "Southwest By-Pass" into "Pass". This method is used to filter out
     * these cases.
     */
    private boolean shouldStandardize(final String name)
    {
        if (Strings.isNaturalNumber(name))
        {
            return false;
        }
        return !name.contains("-");
    }
}
