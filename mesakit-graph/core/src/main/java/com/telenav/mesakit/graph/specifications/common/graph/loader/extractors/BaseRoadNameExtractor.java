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

package com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.core.locale.LanguageIsoCode;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.Split;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.standardizer.RoadNameStandardizer;

import java.util.List;

public abstract class BaseRoadNameExtractor extends BaseExtractor<List<RoadName>, PbfWay>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private RoadNameStandardizer standardizer;

    private final MapLocale locale;

    protected BaseRoadNameExtractor(MapLocale locale, RoadNameStandardizer.Mode mode,
                                    Listener listener)
    {
        super(listener);
        this.locale = locale;

        if (mode != RoadNameStandardizer.Mode.NO_STANDARDIZATION)
        {
            standardizer = RoadNameStandardizer.get(locale, mode);
        }
    }

    protected void addRoadName(List<RoadName> names, String name)
    {
        if (name != null && !name.isBlank())
        {
            if (name.contains(";"))
            {
                for (var subName : Split.split(name, ';'))
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
                        var standardized = standardizer.standardize(roadName);
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

    protected void addRoadNameTranslation(List<RoadName> names, PbfWay way, String key)
    {
        var languages = languageCodes(way);
        for (var language : languages)
        {
            var name = way.tagValue(key + ":" + language.iso3Code());
            if (name != null)
            {
                addRoadName(names, name);
            }
            for (var translation : languages)
            {
                if (translation != language)
                {
                    var translated = way.tagValue(key + ":" + language.iso3Code() + ":trans:" + translation.iso3Code());
                    if (translated != null)
                    {
                        addRoadName(names, translated);
                    }
                }
            }
        }
    }

    protected void addRoadNameTranslations(List<RoadName> names, PbfWay way, String[] keys)
    {
        for (var key : keys)
        {
            addRoadNameTranslation(names, way, key);
        }
    }

    protected List<LanguageIsoCode> languageCodes(PbfWay way)
    {
        // Get any ISO code from the way
        var iso = way.tagValue("iso");
        if (iso != null)
        {
            // then get the country for the iso code,
            var country = Country.forIsoCode(iso);
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
    private boolean shouldStandardize(String name)
    {
        if (Strings.isNaturalNumber(name))
        {
            return false;
        }
        return !name.contains("-");
    }
}
