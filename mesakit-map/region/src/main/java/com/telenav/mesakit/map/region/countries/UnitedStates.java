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

package com.telenav.mesakit.map.region.countries;

import com.telenav.kivakit.core.language.locales.CountryIsoCode;
import com.telenav.kivakit.core.language.locales.LanguageIsoCode;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.countries.states.California;
import com.telenav.mesakit.map.region.countries.states.NewYork;
import com.telenav.mesakit.map.region.countries.states.Washington;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.State;

public class UnitedStates extends Country
{
    public State ALABAMA;

    public State ARIZONA;

    public State ALASKA;

    public State ARKANSAS;

    public California CALIFORNIA;

    public State COLORADO;

    public State CONNECTICUT;

    public State DELAWARE;

    public State DISTRICT_OF_COLUMBIA;

    public State FLORIDA;

    public State GEORGIA;

    public State HAWAII;

    public State IDAHO;

    public State ILLINOIS;

    public State INDIANA;

    public State IOWA;

    public State KANSAS;

    public State KENTUCKY;

    public State LOUISIANA;

    public State MAINE;

    public State MARYLAND;

    public State MASSACHUSETTS;

    public State MICHIGAN;

    public State MINNESOTA;

    public State MISSISSIPPI;

    public State MISSOURI;

    public State MONTANA;

    public State NEBRASKA;

    public State NEVADA;

    public State NEW_HAMPSHIRE;

    public State NEW_JERSEY;

    public State NEW_MEXICO;

    public NewYork NEW_YORK;

    public State NORTH_CAROLINA;

    public State NORTH_DAKOTA;

    public State OHIO;

    public State OKLAHOMA;

    public State OREGON;

    public State PENNSYLVANIA;

    public State PUERTO_RICO;

    public State RHODE_ISLAND;

    public State SOUTH_CAROLINA;

    public State SOUTH_DAKOTA;

    public State TENNESSEE;

    public State TEXAS;

    public State UTAH;

    public State VERMONT;

    public State VIRGINIA;

    public Washington WASHINGTON;

    public State WEST_VIRGINIA;

    public State WISCONSIN;

    public State WYOMING;

    public UnitedStates()
    {
        super(Continent.NORTH_AMERICA, new RegionInstance<>(Country.class)
                .withOrdinal(229)
                .withIdentity(new RegionIdentity("United States")
                        .withCountryTmcCode(new CountryTmcCode(0x01))
                        .withCountryIsoCode(CountryIsoCode.UNITED_STATES))
                .withLanguage(LanguageIsoCode.ENGLISH)
                .withLanguage(LanguageIsoCode.SPANISH)
                .withAutomotiveSupportLevel(AutomotiveSupportLevel.SUPPORTED)
                .withDrivingSide(DrivingSide.RIGHT));
    }

    @Override
    public void onInitialize()
    {
        var BASE = new RegionInstance<>(State.class);

        CALIFORNIA = new California();
        CALIFORNIA.initialize();

        NEW_YORK = new NewYork();
        NEW_YORK.initialize();

        WASHINGTON = new Washington();
        WASHINGTON.initialize();

        ALABAMA = new State(this, BASE
                .withIdentity(new RegionIdentity("Alabama")
                        .withIsoCode("AL")));

        ARIZONA = new State(this, BASE
                .withIdentity(new RegionIdentity("Arizona")
                        .withIsoCode("AZ")));

        ALASKA = new State(this, BASE
                .withIdentity(new RegionIdentity("Alaska")
                        .withIsoCode("AK")));

        ARKANSAS = new State(this, BASE
                .withIdentity(new RegionIdentity("Arkansas")
                        .withIsoCode("AR")));

        COLORADO = new State(this, BASE
                .withIdentity(new RegionIdentity("Colorado")
                        .withIsoCode("CO")));

        CONNECTICUT = new State(this, BASE
                .withIdentity(new RegionIdentity("Connecticut")
                        .withIsoCode("CT")));

        DELAWARE = new State(this, BASE
                .withIdentity(new RegionIdentity("Delaware")
                        .withIsoCode("DE")));

        DISTRICT_OF_COLUMBIA = new State(this, BASE
                .withIdentity(new RegionIdentity("District of Columbia")
                        .withIsoCode("DC")));

        FLORIDA = new State(this, BASE
                .withIdentity(new RegionIdentity("Florida")
                        .withIsoCode("FL")));

        GEORGIA = new State(this, BASE
                .withIdentity(new RegionIdentity("Georgia")
                        .withIsoCode("GA")));

        HAWAII = new State(this, BASE
                .withIdentity(new RegionIdentity("Hawaii")
                        .withIsoCode("HI")));

        IDAHO = new State(this, BASE
                .withIdentity(new RegionIdentity("Idaho")
                        .withIsoCode("ID")));

        ILLINOIS = new State(this, BASE
                .withIdentity(new RegionIdentity("Illinois")
                        .withIsoCode("IL")));

        INDIANA = new State(this, BASE
                .withIdentity(new RegionIdentity("Indiana")
                        .withIsoCode("IN")));

        IOWA = new State(this, BASE
                .withIdentity(new RegionIdentity("Iowa")
                        .withIsoCode("IA")));

        KANSAS = new State(this, BASE
                .withIdentity(new RegionIdentity("Kansas")
                        .withIsoCode("KS")));

        KENTUCKY = new State(this, BASE
                .withIdentity(new RegionIdentity("Kentucky")
                        .withIsoCode("KY")));

        LOUISIANA = new State(this, BASE
                .withIdentity(new RegionIdentity("Louisiana")
                        .withIsoCode("LA")));

        MAINE = new State(this, BASE
                .withIdentity(new RegionIdentity("Maine")
                        .withIsoCode("ME")));

        MARYLAND = new State(this, BASE
                .withIdentity(new RegionIdentity("Maryland")
                        .withIsoCode("MD")));

        MASSACHUSETTS = new State(this, BASE
                .withIdentity(new RegionIdentity("Massachusetts")
                        .withIsoCode("MA")));

        MICHIGAN = new State(this, BASE
                .withIdentity(new RegionIdentity("Michigan")
                        .withIsoCode("MI")));

        MINNESOTA = new State(this, BASE
                .withIdentity(new RegionIdentity("Minnesota")
                        .withIsoCode("MN")));

        MISSISSIPPI = new State(this, BASE
                .withIdentity(new RegionIdentity("Mississippi")
                        .withIsoCode("MS")));

        MISSOURI = new State(this, BASE
                .withIdentity(new RegionIdentity("Missouri")
                        .withIsoCode("MO")));

        MONTANA = new State(this, BASE
                .withIdentity(new RegionIdentity("Montana")
                        .withIsoCode("MT")));

        NEBRASKA = new State(this, BASE
                .withIdentity(new RegionIdentity("Nebraska")
                        .withIsoCode("NE")));

        NEVADA = new State(this, BASE
                .withIdentity(new RegionIdentity("Nevada")
                        .withIsoCode("NV")));

        NEW_HAMPSHIRE = new State(this, BASE
                .withIdentity(new RegionIdentity("New Hampshire")
                        .withIsoCode("NH")));

        NEW_JERSEY = new State(this, BASE
                .withIdentity(new RegionIdentity("New Jersey")
                        .withIsoCode("NJ")));

        NEW_MEXICO = new State(this, BASE
                .withIdentity(new RegionIdentity("New Mexico")
                        .withIsoCode("NM")));

        NORTH_CAROLINA = new State(this, BASE
                .withIdentity(new RegionIdentity("North Carolina")
                        .withIsoCode("NC")));

        NORTH_DAKOTA = new State(this, BASE
                .withIdentity(new RegionIdentity("North Dakota")
                        .withIsoCode("ND")));

        OHIO = new State(this, BASE
                .withIdentity(new RegionIdentity("Ohio")
                        .withIsoCode("OH")));

        OKLAHOMA = new State(this, BASE
                .withIdentity(new RegionIdentity("Oklahoma")
                        .withIsoCode("OK")));

        OREGON = new State(this, BASE
                .withIdentity(new RegionIdentity("Oregon")
                        .withIsoCode("OR")));

        PENNSYLVANIA = new State(this, BASE
                .withIdentity(new RegionIdentity("Pennsylvania")
                        .withIsoCode("PA")));

        PUERTO_RICO = new State(this, BASE
                .withIdentity(new RegionIdentity("Puerto Rico")
                        .withIsoCode("PR")));

        RHODE_ISLAND = new State(this, BASE
                .withIdentity(new RegionIdentity("Rhode Island")
                        .withIsoCode("RI")));

        SOUTH_CAROLINA = new State(this, BASE
                .withIdentity(new RegionIdentity("South Carolina")
                        .withIsoCode("SC")));

        SOUTH_DAKOTA = new State(this, BASE
                .withIdentity(new RegionIdentity("South Dakota")
                        .withIsoCode("SD")));

        TENNESSEE = new State(this, BASE
                .withIdentity(new RegionIdentity("Tennessee")
                        .withIsoCode("TN")));

        TEXAS = new State(this, BASE
                .withIdentity(new RegionIdentity("Texas")
                        .withIsoCode("TX")));

        UTAH = new State(this, BASE
                .withIdentity(new RegionIdentity("Utah")
                        .withIsoCode("UT")));

        VERMONT = new State(this, BASE
                .withIdentity(new RegionIdentity("Vermont")
                        .withIsoCode("VT")));

        VIRGINIA = new State(this, BASE
                .withIdentity(new RegionIdentity("Virginia")
                        .withIsoCode("VA")));

        WEST_VIRGINIA = new State(this, BASE
                .withIdentity(new RegionIdentity("West Virginia")
                        .withIsoCode("WV")));

        WISCONSIN = new State(this, BASE
                .withIdentity(new RegionIdentity("Wisconsin")
                        .withIsoCode("WI")));

        WYOMING = new State(this, BASE
                .withIdentity(new RegionIdentity("Wyoming")
                        .withIsoCode("WY")));
    }
}
