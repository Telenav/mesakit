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

package com.telenav.mesakit.map.region;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.string.Strip;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.region.project.lexakai.DiagramRegion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

/**
 * Represents a hyphenated region or ISO code with up to four parts.
 *
 * @author jonathanl (shibo)
 * @see RegionIdentity
 */
@UmlClassDiagram(diagram = DiagramRegion.class)
public class RegionCode
{
    private static final Set<String> CONTINENTS = new HashSet<>(Arrays.asList("AF", "AFRICA", "AN", "ANTARCTICA", "NA",
            "NORTH_AMERICA", "SA", "SOUTH_AMERICA", "AS", "ASIA", "OC", "OCEANIA", "EU", "EUROPE"));

    private static final Pattern ISO = Pattern.compile("[A-Z0-9_-]+");

    private static final String ISO_CLASS = "[A-Z0-9_]";

    // NOTE: that the dash here is an em-dash(–), not an en-dash (-).
    private static final String MESAKIT_CLASS = "[\\p{Alnum}().`'_\\[\\]–]";

    private static Pattern ISO_PATTERN;

    private static Pattern MESAKIT_PATTERN;

    public static RegionCode parse(String... parts)
    {
        if (parts != null && parts.length > 0)
        {
            // If we have only one string,
            String[] normalized;
            if (parts.length == 1)
            {
                // normalize because of dirty source data
                var code = normalizeName(parts[0]);

                // and split into parts
                normalized = code.split("-");
            }
            else
            {
                // otherwise normalize each part
                normalized = new String[parts.length];
                var index = 0;
                for (var part : parts)
                {
                    normalized[index++] = normalizeName(part);
                }
            }

            // and if we have 4 or fewer parts,
            if (normalized.length <= 4)
            {
                // and it is an iso or mesakit code,
                var code = new RegionCode(normalized);
                if (code.isIso() || code.isMesaKitn())
                {
                    // then return it
                    return code;
                }
            }
        }

        return null;
    }

    private String[] parts;

    private RegionCode(String... parts)
    {
        this.parts = parts;
    }

    private RegionCode()
    {
    }

    public RegionCode aonized()
    {
        return RegionCode.parse(aonize(code()));
    }

    public RegionCode append(RegionCode that)
    {
        var copy = new RegionCode();
        copy.parts = new String[parts.length + that.parts.length];
        System.arraycopy(parts, 0, copy.parts, 0, parts.length);
        System.arraycopy(that.parts, 0, copy.parts, parts.length, that.parts.length);
        ensure(copy.parts.length <= 4);
        return copy;
    }

    public String code()
    {
        return new StringList(Maximum._4, parts).join('-');
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof RegionCode)
        {
            var that = (RegionCode) object;
            return Arrays.equals(parts, that.parts);
        }
        return false;
    }

    public RegionCode first()
    {
        return get(0);
    }

    public RegionCode first(int count)
    {
        return RegionCode.parse(new StringList(Maximum._4, parts).first(Count.count(count)).join('-'));
    }

    public RegionCode fourth()
    {
        return get(3);
    }

    public boolean hasCity()
    {
        return size() >= 3 && third().startsWithIgnoreCase("CITY");
    }

    public boolean hasCountry()
    {
        return isCountry();
    }

    public boolean hasCounty()
    {
        return size() >= 3 && third().startsWithIgnoreCase("COUNTY");
    }

    public boolean hasDistrict()
    {
        return size() == 4 && fourth().startsWithIgnoreCase("DISTRICT");
    }

    public boolean hasMetropolitanArea()
    {
        return size() >= 3 && third().startsWithIgnoreCase("METRO");
    }

    public boolean hasState()
    {
        return size() >= 2;
    }

    public boolean hasTimeZone()
    {
        return size() == 1 && first().startsWithIgnoreCase("TIMEZONE");
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(parts);
    }

    public boolean isCity()
    {
        return size() == 3 && hasState() && hasCity();
    }

    public boolean isContinent()
    {
        return size() == 1 && CONTINENTS.contains(first().code().toUpperCase());
    }

    public boolean isCountry()
    {
        return size() == 1 && !isWorld() && !isContinent() && !isTimeZone();
    }

    public boolean isCounty()
    {
        return size() == 3 && hasState() && hasCounty();
    }

    public boolean isDistrict()
    {
        return hasCity() && hasDistrict();
    }

    public boolean isIso()
    {
        return ISO.matcher(code()).matches();
    }

    public boolean isMesaKitn()
    {
        return !isIso();
    }

    public boolean isMetropolitanArea()
    {
        return size() == 3 && hasState() && hasMetropolitanArea();
    }

    public boolean isState()
    {
        return size() == 2;
    }

    public boolean isTimeZone()
    {
        return size() == 1 && hasTimeZone();
    }

    public boolean isValid()
    {
        if (isIso())
        {
            return ISO_PATTERN().matcher(code()).matches();
        }
        else
        {
            return MESAKIT_PATTERN().matcher(code()).matches();
        }
    }

    public boolean isWorld()
    {
        return size() == 1 && "EARTH".equalsIgnoreCase(first().code());
    }

    public RegionCode isoized()
    {
        return RegionCode.parse(isoize(code()));
    }

    public RegionCode last()
    {
        if (size() > 0)
        {
            return get(size() - 1);
        }
        return null;
    }

    public RegionCode nameized()
    {
        return RegionCode.parse(nameize(code()));
    }

    public RegionCode normalized()
    {
        return RegionCode.parse(normalizeName(code()));
    }

    public RegionCode second()
    {
        return get(1);
    }

    public int size()
    {
        return parts.length;
    }

    public boolean startsWithIgnoreCase(String prefix)
    {
        return Strings.startsWithIgnoreCase(parts[0], prefix);
    }

    public RegionCode third()
    {
        return get(2);
    }

    @Override
    public String toString()
    {
        return code();
    }

    public RegionCode withoutPrefix(String prefix)
    {
        ensure(!Strings.isEmpty(prefix));
        var code = Strip.leading(code(), prefix);
        code = Strip.leading(code, "_");
        return RegionCode.parse(code);
    }

    private static Pattern ISO_PATTERN()
    {
        if (ISO_PATTERN == null)
        {
            ISO_PATTERN = Pattern.compile(
                    "(?<world>EARTH|MARS)"
                            + "|"
                            + "(?<continent>AF|AN|AS|EU|NA|OC|SA)"
                            + "|"
                            + "(?<timezone>TIMEZONE_" + ISO_CLASS + "+)"
                            + "|"
                            + "((?<country>[A-Z]{2,3})"
                            + "(-(?<state>" + ISO_CLASS + "+?))?"
                            + "(-(?<metro>METRO_" + ISO_CLASS + "+))?"
                            + "(-(?<county>COUNTY_" + ISO_CLASS + "+))?"
                            + "(-(?<city>CITY_" + ISO_CLASS + "+))?"
                            + "(-(?<district>DISTRICT" + ISO_CLASS + "+))?)");
        }
        return ISO_PATTERN;
    }

    private static Pattern MESAKIT_PATTERN()
    {
        if (MESAKIT_PATTERN == null)
        {
            MESAKIT_PATTERN = Pattern.compile(
                    "(?<world>Earth|Mars)"
                            + "|"
                            + "(?<continent>Africa|Antarctica|Asia|Europe|North_America|Oceania|South_America)"
                            + "|"
                            + "(?<timezone>TimeZone_" + MESAKIT_CLASS + "+)"
                            + "|"
                            + "((?<country>[A-Za-z_]+)"
                            + "(-(?<state>" + MESAKIT_CLASS + "+?))?"
                            + "(-(?<metro>Metro_" + MESAKIT_CLASS + "+))?"
                            + "(-(?<county>County_" + MESAKIT_CLASS + "+))?"
                            + "(-(?<city>City_" + MESAKIT_CLASS + "+))?"
                            + "(-(?<district>District_" + MESAKIT_CLASS + "+))?)",
                    Pattern.UNICODE_CHARACTER_CLASS);
        }
        return MESAKIT_PATTERN;
    }

    /**
     * Normalized the string and makes it compatible with the filesystem
     */
    private static String aonize(String name)
    {
        return normalizeName(name).replaceAll("[,:;' ]", "_");
    }

    /**
     * Makes a string into a fake ISO code by converting to upper-case and removing all characters except letters,
     * digits and dashes.
     */
    private static String isoize(String code)
    {
        var normalized = normalizeName(code.toUpperCase());
        var builder = new StringBuilder();
        for (var i = 0; i < normalized.length(); i++)
        {
            var at = normalized.charAt(i);
            if ((at >= 'A' && at <= 'Z') || (at >= '0' && at <= '9') || at == '-')
            {
                builder.append(at);
            }
        }
        return builder.toString();
    }

    /**
     * @return Turns a raw name string into one that's compatible with region codes
     */
    private static String nameize(String name)
    {
        return name.replaceAll("-", "_");
    }

    /**
     * Strips any trailing dash and turns irregular dashes into em-dashes
     */
    private static String normalizeName(String name)
    {
        assert name != null;

        // HOTSPOT: This method has been determined to be a hotspot by YourKit profiling

        name = Strip.ending(name, "-");
        name = Strings.replaceAll(name, "--", "\u2014");
        name = Strings.replaceAll(name, " - ", "\u2014");
        return name;
    }

    RegionCode get(int index)
    {
        if (index < parts.length)
        {
            return RegionCode.parse(parts[index]);
        }
        return null;
    }
}
