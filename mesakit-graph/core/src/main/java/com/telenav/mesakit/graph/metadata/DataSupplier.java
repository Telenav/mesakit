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

package com.telenav.mesakit.graph.metadata;

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.commandline.SwitchParsers;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.geography.Precision;

/**
 * Suppliers of map data (independent of format)
 *
 * @author jonathanl (shibo)
 */
public enum DataSupplier implements Matcher<DataSupplier>
{
    ALL(Precision.NONE), // Matches all data suppliers
    Telenav(Precision.DM7), // Telenav
    OSM(Precision.DM6), // OpenStreetMap (https://www.openstreetmap.org)
    HERE(Precision.DM6), // HERE (https://www.here.com)
    TomTom(Precision.DM6); // TomTom (https://www.tomtom.com/)

    public static SwitchParser.Builder<DataSupplier> switchParser(Listener listener)
    {
        return SwitchParsers
                .enumSwitchParser(listener, "data-supplier", "The name of the data supplier", DataSupplier.class)
                .defaultValue(HERE);
    }

    private final Precision precision;

    DataSupplier(Precision precision)
    {
        this.precision = precision;
    }

    public boolean isHere()
    {
        return equals(HERE);
    }

    public boolean isOsm()
    {
        return equals(OSM);
    }

    public boolean isTomTom()
    {
        return equals(OSM);
    }

    @Override
    public boolean matches(DataSupplier supplier)
    {
        switch (this)
        {
            case ALL:
                return true;

            case OSM:
                return OSM == supplier;

            case HERE:
                return HERE == supplier;

            default:
                return false;
        }
    }

    public Precision precision()
    {
        return precision;
    }
}
