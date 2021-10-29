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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.telenav.kivakit.kernel.language.locales.CountryIsoCode;
import com.telenav.kivakit.kernel.language.reflection.property.KivaKitExcludeProperty;
import com.telenav.kivakit.kernel.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramRegion;
import com.telenav.mesakit.map.region.regions.City;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.mesakit.map.region.regions.TimeZone;
import org.junit.Before;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * <p>
 * Holds all coding information about an MesaKit {@link Region}.
 * </p>
 * All regions are assigned an MesaKit region code as well as an ISO code or a synthetic ISO code.
 *
 * <pre>
 *
 * [world]
 *
 *  mesakit: World
 *  iso: WORLD
 *
 * [continent]
 *
 *  mesakit: Oceania
 *  iso: OC
 *
 * [time-zone]
 *
 *  iso: America/Seattle
 *
 * [country]
 *
 *  mesakit: United_States
 *  iso: US
 *
 * [country]-[state]
 *
 *  mesakit: Mexico-Aguascalientes
 *  iso: MX-AG
 *
 * [country]-[state]-Metro_[metro]
 *
 *  mesakit: United_States-Washington-Metro_Seattle
 *  iso: US-WA-METRO_SEATTLE
 *
 * [country]-[state]-County_[county]
 *
 *  mesakit:United_States-Washington-County_King
 *  iso: US-WA-COUNTY_KING
 *
 * [country]-[state]-City_[city]
 *
 *  mesakit: United_States-Washington-City_Seattle
 *  iso: US-WA-CITY_SEATTLE
 *
 * [country]-[state]-City-[city]-District_[district]
 *  mesakit: United_States-Washington-City_Seattle-District_Green_Lake
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

    @UmlAggregation(label = "mesakit")
    private RegionCode mesakit;

    @UmlAggregation
    private RegionIdentifier identifier;

    private int countryOrdinal;

    private Country.CountryTmcCode countryTmcCode;

    private CountryIsoCode countryIsoCode;

    public RegionIdentity()
    {
    }

    public RegionIdentity(RegionIdentity that)
    {
        identifier = that.identifier;
        name = that.name;
        iso = that.iso;
        mesakit = that.mesakit;

        countryOrdinal = that.countryOrdinal;
        countryTmcCode = that.countryTmcCode;
        countryIsoCode = that.countryIsoCode;
    }

    public RegionIdentity(String name)
    {
        name(name);
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
    public boolean equals(Object object)
    {
        if (object instanceof RegionIdentity)
        {
            var that = (RegionIdentity) object;
            if (hasIsoCode() && that.hasIsoCode())
            {
                return iso().equals(that.iso());
            }
            if (hasMesaKitCode() && that.hasMesaKitCode())
            {
                return mesakit().equals(that.mesakit());
            }
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    public <T extends Region<T>> T findOrCreateRegion(Class<T> type)
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
            var country = country();
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
            var state = first(2).state();
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
            var state = first(2).state();
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

    public RegionIdentity first(int n)
    {
        var copy = new RegionIdentity(this);
        copy.mesakit = mesakit().first(n);
        copy.iso = iso().first(n);
        return copy;
    }

    public boolean hasIdentifier()
    {
        return identifier() != null;
    }

    public boolean hasIsoCode()
    {
        return iso() != null;
    }

    public boolean hasMesaKitCode()
    {
        return mesakit() != null;
    }

    @Override
    public int hashCode()
    {
        if (hasIsoCode())
        {
            return iso().hashCode();
        }
        if (hasMesaKitCode())
        {
            return mesakit().hashCode();
        }
        throw new IllegalStateException();
    }

    @KivaKitIncludeProperty
    public final RegionIdentifier identifier()
    {
        return identifier;
    }

    public RegionIdentity identifier(RegionIdentifier identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @KivaKitExcludeProperty
    public boolean isCity()
    {
        return (hasIsoCode() && iso().isCity()) || (hasMesaKitCode() && mesakit().isCity());
    }

    @KivaKitExcludeProperty
    public boolean isContinent()
    {
        return (hasIsoCode() && iso().isContinent()) || (hasMesaKitCode() && mesakit().isContinent());
    }

    @KivaKitExcludeProperty
    public boolean isCountry()
    {
        return (hasIsoCode() && iso().isCountry()) || (hasMesaKitCode() && mesakit().isCountry());
    }

    @KivaKitExcludeProperty
    public boolean isCounty()
    {
        return (hasIsoCode() && iso().isCounty()) || (hasMesaKitCode() && mesakit().isCounty());
    }

    @KivaKitExcludeProperty
    public boolean isDistrict()
    {
        return (hasIsoCode() && iso().isDistrict()) || (hasMesaKitCode() && mesakit().isDistrict());
    }

    @KivaKitExcludeProperty
    public boolean isMetropolitanArea()
    {
        return (hasIsoCode() && iso().isMetropolitanArea()) || (hasMesaKitCode() && mesakit().isMetropolitanArea());
    }

    @KivaKitExcludeProperty
    public boolean isState()
    {
        return (hasIsoCode() && iso().isState()) || (hasMesaKitCode() && mesakit().isState());
    }

    @KivaKitExcludeProperty
    public boolean isTimeZone()
    {
        return (hasIsoCode() && iso().isTimeZone()) || (hasMesaKitCode() && mesakit().isTimeZone());
    }

    @KivaKitExcludeProperty
    public boolean isValid()
    {
        return hasName() && (hasIsoCode() || hasMesaKitCode() && hasIdentifier());
    }

    @KivaKitExcludeProperty
    public boolean isWorld()
    {
        return (hasIsoCode() && iso().isWorld()) || (hasMesaKitCode() && mesakit().isWorld());
    }

    @KivaKitIncludeProperty
    public RegionCode iso()
    {
        return iso;
    }

    public RegionIdentity last()
    {
        var copy = new RegionIdentity(this);
        copy.iso = iso().last();
        copy.mesakit = mesakit().last();
        return copy;
    }

    @KivaKitIncludeProperty
    public RegionCode mesakit()
    {
        return mesakit;
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
    public void read(Kryo kryo, Input input)
    {
        name = kryo.readObject(input, String.class);
        identifier = new RegionIdentifier(kryo.readObject(input, Integer.class));
        iso = RegionCode.parse(kryo.readObject(input, String.class));
        ensureNotNull(iso, "Missing ISO code for $", name);
        mesakit = RegionCode.parse(kryo.readObject(input, String.class));
        ensureNotNull(mesakit, "Missing MesaKit code for $", name);
        countryOrdinal = kryo.readObject(input, Integer.class);
        var tmcCode = kryo.readObjectOrNull(input, Integer.class);
        countryTmcCode = tmcCode == null ? null : new Country.CountryTmcCode(tmcCode);
        var alpha2CountryCode = kryo.readObjectOrNull(input, String.class);
        var alpha3CountryCode = kryo.readObjectOrNull(input, String.class);
        var numericCountryCode = kryo.readObjectOrNull(input, Integer.class);
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
        RegionProject.get().initialize();
    }

    public TimeZone timeZone()
    {
        return TimeZone.forIdentity(this);
    }

    @Override
    public String toString()
    {
        return mesakit.last().code();
    }

    public RegionIdentity unqualified()
    {
        var identity = new RegionIdentity(this);
        identity.mesakit = mesakit().last();
        identity.iso = iso().last();
        return identity;
    }

    public RegionIdentity withCountryIsoCode(CountryIsoCode code)
    {
        var copy = new RegionIdentity(this);
        copy.countryIsoCode = code;
        copy.iso = RegionCode.parse(code.alpha2Code());
        return copy;
    }

    public RegionIdentity withCountryOrdinal(int ordinal)
    {
        var copy = new RegionIdentity(this);
        copy.countryOrdinal = ordinal;
        return copy;
    }

    public RegionIdentity withCountryTmcCode(Country.CountryTmcCode code)
    {
        var copy = new RegionIdentity(this);
        copy.countryTmcCode = code;
        return copy;
    }

    public RegionIdentity withIdentifier(RegionIdentifier identifier)
    {
        return new RegionIdentity(this).identifier(identifier);
    }

    public RegionIdentity withIsoCode(RegionCode code)
    {
        var copy = new RegionIdentity(this);
        copy.iso = code;
        return copy;
    }

    /**
     * Handles continent ISO codes like "OC" and "NA", country ISO codes like "US" and "MX", state ISO codes like
     * "US-WA" and "US-NM" and metro area codes like "MX-CH-CHIHUAHUA"
     */
    public RegionIdentity withIsoCode(String code)
    {
        return new RegionIdentity(this).iso(code);
    }

    public RegionIdentity withMesaKitCode(RegionCode code)
    {
        var copy = new RegionIdentity(this);
        copy.mesakit = code;
        return copy;
    }

    public RegionIdentity withMesaKitCode(String code)
    {
        return new RegionIdentity(this).mesakit(code);
    }

    public RegionIdentity withName(String name)
    {
        return new RegionIdentity(this).name(name);
    }

    public RegionIdentity withPrefix(RegionIdentity identity)
    {
        assertNonNullAndNonEmpty(identity);
        ensure(identity.isValid());

        var copy = new RegionIdentity(this);
        if (hasIsoCode() && identity.hasIsoCode())
        {
            copy.iso = identity.iso().append(iso());
        }
        if (hasMesaKitCode() && identity.hasMesaKitCode())
        {
            copy.mesakit = identity.mesakit().append(mesakit());
        }
        return copy;
    }

    public RegionIdentity withPrefix(String prefix)
    {
        assertNonNullAndNonEmpty(prefix);

        var copy = new RegionIdentity(this);
        if (hasIsoCode())
        {
            copy.iso = RegionCode.parse(prefix.toUpperCase() + iso().code());
        }
        if (hasMesaKitCode())
        {
            copy.mesakit = RegionCode.parse(prefix + "_" + mesakit().code());
        }
        return copy;
    }

    public RegionIdentity withoutPrefix(String prefix)
    {
        assertNonNullAndNonEmpty(prefix);

        var copy = new RegionIdentity(this);
        if (hasIsoCode())
        {
            copy.iso = iso().withoutPrefix(prefix.toUpperCase());
        }
        if (hasMesaKitCode())
        {
            copy.mesakit = mesakit().withoutPrefix(prefix);
        }
        return copy;
    }

    @Override
    public void write(Kryo kryo, Output output)
    {
        kryo.writeObject(output, name());
        kryo.writeObject(output, identifier().asInt());
        kryo.writeObject(output, iso().code());
        kryo.writeObject(output, mesakit().code());
        kryo.writeObject(output, countryOrdinal());
        kryo.writeObjectOrNull(output, countryTmcCode() == null ? null : countryTmcCode().asInt(), Integer.class);
        kryo.writeObjectOrNull(output, countryIsoCode() == null ? null : countryIsoCode().alpha2Code(), String.class);
        kryo.writeObjectOrNull(output, countryIsoCode() == null ? null : countryIsoCode().alpha3Code(), String.class);
        kryo.writeObjectOrNull(output, countryIsoCode() == null ? null : countryIsoCode().numericCountryCode(), Integer.class);
    }

    RegionIdentity name(String name)
    {
        this.name = assertNonNullAndNonEmpty(name);
        if (!hasMesaKitCode())
        {
            mesakit(name);
        }
        if (!hasIsoCode())
        {
            iso(name);
        }
        return this;
    }

    private <T> T assertNonNullAndNonEmpty(T object)
    {
        ensure(object != null);
        if (object instanceof String)
        {
            var string = (String) object;
            ensure(!string.contains("null") && !string.contains("NULL"));
            ensure(!"".equals(string));
        }
        return object;
    }

    private boolean hasName()
    {
        return name() != null;
    }

    private RegionIdentity iso(String string)
    {
        assertNonNullAndNonEmpty(string);
        var code = RegionCode.parse(string);
        if (code != null)
        {
            iso = code.isoized();
        }
        return this;
    }

    private RegionIdentity mesakit(String string)
    {
        assertNonNullAndNonEmpty(string);
        var code = RegionCode.parse(string);
        if (code != null)
        {
            mesakit = code.aonized();
        }
        return this;
    }
}
