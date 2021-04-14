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

package com.telenav.aonia.map.region;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.telenav.aonia.map.region.project.MapRegionProject;
import com.telenav.aonia.map.region.project.lexakai.diagrams.DiagramRegion;
import com.telenav.aonia.map.region.regions.City;
import com.telenav.aonia.map.region.regions.Continent;
import com.telenav.aonia.map.region.regions.Country;
import com.telenav.aonia.map.region.regions.County;
import com.telenav.aonia.map.region.regions.MetropolitanArea;
import com.telenav.aonia.map.region.regions.State;
import com.telenav.aonia.map.region.regions.TimeZone;
import com.telenav.kivakit.core.kernel.language.locales.CountryIsoCode;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitExcludeProperty;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import org.junit.Before;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * <p>
 * Holds all coding information about an Aonia {@link Region}.
 * </p>
 * All regions are assigned an Aonia region code as well as an ISO code or a synthetic ISO code.
 *
 * <pre>
 *
 * [world]
 *
 *  aonia: World
 *  iso: WORLD
 *
 * [continent]
 *
 *  aonia: Oceania
 *  iso: OC
 *
 * [time-zone]
 *
 *  iso: America/Seattle
 *
 * [country]
 *
 *  aonia: United_States
 *  iso: US
 *
 * [country]-[state]
 *
 *  aonia: Mexico-Aguascalientes
 *  iso: MX-AG
 *
 * [country]-[state]-Metro_[metro]
 *
 *  aonia: United_States-Washington-Metro_Seattle
 *  iso: US-WA-METRO_SEATTLE
 *
 * [country]-[state]-County_[county]
 *
 *  aonia:United_States-Washington-County_King
 *  iso: US-WA-COUNTY_KING
 *
 * [country]-[state]-City_[city]
 *
 *  aonia: United_States-Washington-City_Seattle
 *  iso: US-WA-CITY_SEATTLE
 *
 * [country]-[state]-City-[city]-District_[district]
 *  aonia: United_States-Washington-City_Seattle-District_Green_Lake
 *  iso: US-WA-CITY_SEATTLE-DISTRICT_GREEN_LAKE
 * </pre>
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramRegion.class)
@UmlExcludeSuperTypes
public class RegionIdentity implements AsString, KryoSerializable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private String name;

    @UmlAggregation(label = "iso")
    private RegionCode iso;

    @UmlAggregation(label = "aonia")
    private RegionCode aonia;

    @UmlAggregation
    private RegionIdentifier identifier;

    private int countryOrdinal;

    private Country.CountryTmcCode countryTmcCode;

    private CountryIsoCode countryIsoCode;

    public RegionIdentity()
    {
    }

    public RegionIdentity(final RegionIdentity that)
    {
        identifier = that.identifier;
        name = that.name;
        iso = that.iso;
        aonia = that.aonia;

        countryOrdinal = that.countryOrdinal;
        countryTmcCode = that.countryTmcCode;
        countryIsoCode = that.countryIsoCode;
    }

    public RegionIdentity(final String name)
    {
        name(name);
    }

    @KivaKitIncludeProperty
    public RegionCode aonia()
    {
        return aonia;
    }

    @Override
    public String asString()
    {
        return new ObjectFormatter(this).toString();
    }

    public City city()
    {
        return City.forIdentity(this);
    }

    public Continent continent()
    {
        return Continent.forIdentity(this);
    }

    public Country country()
    {
        return Country.forIdentity(this);
    }

    @KivaKitIncludeProperty
    public CountryIsoCode countryIsoCode()
    {
        return countryIsoCode;
    }

    public int countryOrdinal()
    {
        return countryOrdinal;
    }

    @KivaKitIncludeProperty
    public Country.CountryTmcCode countryTmcCode()
    {
        return countryTmcCode;
    }

    public County county()
    {
        return County.forIdentity(this);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RegionIdentity)
        {
            final var that = (RegionIdentity) object;
            if (hasIsoCode() && that.hasIsoCode())
            {
                return iso().equals(that.iso());
            }
            if (hasAoniaCode() && that.hasAoniaCode())
            {
                return aonia().equals(that.aonia());
            }
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    public <T extends Region<T>> T findOrCreateRegion(final Class<T> type)
    {
        if (type.isAssignableFrom(Country.class))
        {
            return (T) Country.forIdentity(this);
        }
        if (type.isAssignableFrom(Continent.class))
        {
            return (T) Continent.forIdentity(this);
        }
        if (type.isAssignableFrom(TimeZone.class))
        {
            var zone = TimeZone.forIdentity(this);
            if (zone == null)
            {
                zone = new TimeZone(new RegionInstance<>(TimeZone.class)
                        .withIdentity(withoutPrefix("TimeZone")));
            }
            return (T) zone;
        }
        if (type.isAssignableFrom(State.class))
        {
            final var country = country();
            if (country != null)
            {
                var state = State.forIdentity(this);
                if (state == null)
                {
                    state = new State(country, new RegionInstance<>(State.class)
                            .withIdentity(unqualified().withoutPrefix("State")));
                }
                return (T) state;
            }
        }
        if (type.isAssignableFrom(MetropolitanArea.class))
        {
            final var state = first(2).state();
            if (state != null)
            {
                var area = MetropolitanArea.forIdentity(this);
                if (area == null)
                {
                    area = new MetropolitanArea(state, new RegionInstance<>(MetropolitanArea.class)
                            .withIdentity(unqualified().withoutPrefix("Metro")));
                }
                return (T) area;
            }
        }
        if (type.isAssignableFrom(County.class))
        {
            final var state = first(2).state();
            if (state != null)
            {
                var county = County.forIdentity(this);
                if (county == null)
                {
                    county = new County(state, new RegionInstance<>(County.class)
                            .withIdentity(unqualified().withoutPrefix("County")));
                }
                return (T) county;
            }
        }
        LOGGER.warning("Unable to find or create $ with identity $", type.getSimpleName(), this);
        return null;
    }

    public RegionIdentity first(final int n)
    {
        final var copy = new RegionIdentity(this);
        copy.aonia = aonia().first(n);
        copy.iso = iso().first(n);
        return copy;
    }

    public boolean hasAoniaCode()
    {
        return aonia() != null;
    }

    public boolean hasIdentifier()
    {
        return identifier() != null;
    }

    public boolean hasIsoCode()
    {
        return iso() != null;
    }

    @Override
    public int hashCode()
    {
        if (hasIsoCode())
        {
            return iso().hashCode();
        }
        if (hasAoniaCode())
        {
            return aonia().hashCode();
        }
        throw new IllegalStateException();
    }

    @KivaKitIncludeProperty
    public final RegionIdentifier identifier()
    {
        return identifier;
    }

    public RegionIdentity identifier(final RegionIdentifier identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @KivaKitExcludeProperty
    public boolean isCity()
    {
        return (hasIsoCode() && iso().isCity()) || (hasAoniaCode() && aonia().isCity());
    }

    @KivaKitExcludeProperty
    public boolean isContinent()
    {
        return (hasIsoCode() && iso().isContinent()) || (hasAoniaCode() && aonia().isContinent());
    }

    @KivaKitExcludeProperty
    public boolean isCountry()
    {
        return (hasIsoCode() && iso().isCountry()) || (hasAoniaCode() && aonia().isCountry());
    }

    @KivaKitExcludeProperty
    public boolean isCounty()
    {
        return (hasIsoCode() && iso().isCounty()) || (hasAoniaCode() && aonia().isCounty());
    }

    @KivaKitExcludeProperty
    public boolean isDistrict()
    {
        return (hasIsoCode() && iso().isDistrict()) || (hasAoniaCode() && aonia().isDistrict());
    }

    @KivaKitExcludeProperty
    public boolean isMetropolitanArea()
    {
        return (hasIsoCode() && iso().isMetropolitanArea()) || (hasAoniaCode() && aonia().isMetropolitanArea());
    }

    @KivaKitExcludeProperty
    public boolean isState()
    {
        return (hasIsoCode() && iso().isState()) || (hasAoniaCode() && aonia().isState());
    }

    @KivaKitExcludeProperty
    public boolean isTimeZone()
    {
        return (hasIsoCode() && iso().isTimeZone()) || (hasAoniaCode() && aonia().isTimeZone());
    }

    @KivaKitExcludeProperty
    public boolean isValid()
    {
        return hasName() && (hasIsoCode() || hasAoniaCode() && hasIdentifier());
    }

    @KivaKitExcludeProperty
    public boolean isWorld()
    {
        return (hasIsoCode() && iso().isWorld()) || (hasAoniaCode() && aonia().isWorld());
    }

    @KivaKitIncludeProperty
    public RegionCode iso()
    {
        return iso;
    }

    public RegionIdentity last()
    {
        final var copy = new RegionIdentity(this);
        copy.iso = iso().last();
        copy.aonia = aonia().last();
        return copy;
    }

    public MetropolitanArea metropolitanArea()
    {
        return MetropolitanArea.forIdentity(this);
    }

    @KivaKitIncludeProperty
    public String name()
    {
        return name;
    }

    @Override
    public void read(final Kryo kryo, final Input input)
    {
        name = kryo.readObject(input, String.class);
        identifier = new RegionIdentifier(kryo.readObject(input, Integer.class));
        iso = RegionCode.parse(kryo.readObject(input, String.class));
        ensureNotNull(iso, "Missing ISO code for $", name);
        aonia = RegionCode.parse(kryo.readObject(input, String.class));
        ensureNotNull(aonia, "Missing Aonia code for $", name);
        countryOrdinal = kryo.readObject(input, Integer.class);
        final var tmcCode = kryo.readObjectOrNull(input, Integer.class);
        countryTmcCode = tmcCode == null ? null : new Country.CountryTmcCode(tmcCode);
        final var alpha2CountryCode = kryo.readObjectOrNull(input, String.class);
        final var alpha3CountryCode = kryo.readObjectOrNull(input, String.class);
        final var numericCountryCode = kryo.readObjectOrNull(input, Integer.class);
        countryIsoCode = alpha2CountryCode == null ? null :
                new CountryIsoCode(name, alpha2CountryCode, alpha3CountryCode, numericCountryCode);
    }

    public State state()
    {
        return State.forIdentity(this);
    }

    @Before
    public void testSetup()
    {
        MapRegionProject.get().initialize();
    }

    public TimeZone timeZone()
    {
        return TimeZone.forIdentity(this);
    }

    @Override
    public String toString()
    {
        return aonia.last().code();
    }

    public RegionIdentity unqualified()
    {
        final var identity = new RegionIdentity(this);
        identity.aonia = aonia().last();
        identity.iso = iso().last();
        return identity;
    }

    public RegionIdentity withAoniaCode(final RegionCode code)
    {
        final var copy = new RegionIdentity(this);
        copy.aonia = code;
        return copy;
    }

    public RegionIdentity withAoniaCode(final String code)
    {
        return new RegionIdentity(this).aonia(code);
    }

    public RegionIdentity withCountryIsoCode(final CountryIsoCode code)
    {
        final var copy = new RegionIdentity(this);
        copy.countryIsoCode = code;
        copy.iso = RegionCode.parse(code.alpha2Code());
        return copy;
    }

    public RegionIdentity withCountryOrdinal(final int ordinal)
    {
        final var copy = new RegionIdentity(this);
        copy.countryOrdinal = ordinal;
        return copy;
    }

    public RegionIdentity withCountryTmcCode(final Country.CountryTmcCode code)
    {
        final var copy = new RegionIdentity(this);
        copy.countryTmcCode = code;
        return copy;
    }

    public RegionIdentity withIdentifier(final RegionIdentifier identifier)
    {
        return new RegionIdentity(this).identifier(identifier);
    }

    public RegionIdentity withIsoCode(final RegionCode code)
    {
        final var copy = new RegionIdentity(this);
        copy.iso = code;
        return copy;
    }

    /**
     * Handles continent ISO codes like "OC" and "NA", country ISO codes like "US" and "MX", state ISO codes like
     * "US-WA" and "US-NM" and metro area codes like "MX-CH-CHIHUAHUA"
     */
    public RegionIdentity withIsoCode(final String code)
    {
        return new RegionIdentity(this).iso(code);
    }

    public RegionIdentity withName(final String name)
    {
        return new RegionIdentity(this).name(name);
    }

    public RegionIdentity withPrefix(final RegionIdentity identity)
    {
        assertNonNullAndNonEmpty(identity);
        ensure(identity.isValid());

        final var copy = new RegionIdentity(this);
        if (hasIsoCode() && identity.hasIsoCode())
        {
            copy.iso = identity.iso().append(iso());
        }
        if (hasAoniaCode() && identity.hasAoniaCode())
        {
            copy.aonia = identity.aonia().append(aonia());
        }
        return copy;
    }

    public RegionIdentity withPrefix(final String prefix)
    {
        assertNonNullAndNonEmpty(prefix);

        final var copy = new RegionIdentity(this);
        if (hasIsoCode())
        {
            copy.iso = RegionCode.parse(prefix.toUpperCase() + iso().code());
        }
        if (hasAoniaCode())
        {
            copy.aonia = RegionCode.parse(prefix + "_" + aonia().code());
        }
        return copy;
    }

    public RegionIdentity withoutPrefix(final String prefix)
    {
        assertNonNullAndNonEmpty(prefix);

        final var copy = new RegionIdentity(this);
        if (hasIsoCode())
        {
            copy.iso = iso().withoutPrefix(prefix.toUpperCase());
        }
        if (hasAoniaCode())
        {
            copy.aonia = aonia().withoutPrefix(prefix);
        }
        return copy;
    }

    @Override
    public void write(final Kryo kryo, final Output output)
    {
        kryo.writeObject(output, name());
        kryo.writeObject(output, identifier().asInteger());
        kryo.writeObject(output, iso().code());
        kryo.writeObject(output, aonia().code());
        kryo.writeObject(output, countryOrdinal());
        kryo.writeObjectOrNull(output, countryTmcCode() == null ? null : countryTmcCode().asInteger(), Integer.class);
        kryo.writeObjectOrNull(output, countryIsoCode() == null ? null : countryIsoCode().alpha2Code(), String.class);
        kryo.writeObjectOrNull(output, countryIsoCode() == null ? null : countryIsoCode().alpha3Code(), String.class);
        kryo.writeObjectOrNull(output, countryIsoCode() == null ? null : countryIsoCode().numericCountryCode(), Integer.class);
    }

    RegionIdentity name(final String name)
    {
        this.name = assertNonNullAndNonEmpty(name);
        if (!hasAoniaCode())
        {
            aonia(name);
        }
        if (!hasIsoCode())
        {
            iso(name);
        }
        return this;
    }

    private RegionIdentity aonia(final String string)
    {
        assertNonNullAndNonEmpty(string);
        final var code = RegionCode.parse(string);
        if (code != null)
        {
            aonia = code.aonized();
        }
        return this;
    }

    private <T> T assertNonNullAndNonEmpty(final T object)
    {
        ensure(object != null);
        if (object instanceof String)
        {
            final var string = (String) object;
            ensure(!string.contains("null") && !string.contains("NULL"));
            ensure(!"".equals(string));
        }
        return object;
    }

    private boolean hasName()
    {
        return name() != null;
    }

    private RegionIdentity iso(final String string)
    {
        assertNonNullAndNonEmpty(string);
        final var code = RegionCode.parse(string);
        if (code != null)
        {
            iso = code.isoized();
        }
        return this;
    }
}
