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

package com.telenav.mesakit.graph.traffic;

import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.collections.map.string.NameMap;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem;

import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;

public class MapData implements Named
{
    // See MapData_Configuration.groovy for aliases
    // Map vendors that provide TMC traffic are combined under "TomTom"
    public static final MapData TOMTOM = new MapData("TomTom");

    public static final MapData NAVTEQ = new MapData("Navteq");

    public static final MapData OSM = new MapData("OSM");

    public static final MapData AUTONAVI = new MapData("AutoNavi");

    private static final NameMap<MapData> mapDataForName = new NameMap<>(Maximum.maximum(100));

    static
    {
        TOMTOM.add(RoadSectionCodingSystem.TMC);
        TOMTOM.add(RoadSectionCodingSystem.TOMTOM_EDGE_IDENTIFIER);
    }

    static
    {
        NAVTEQ.add(RoadSectionCodingSystem.TMC);
        NAVTEQ.add(RoadSectionCodingSystem.NAVTEQ_EDGE_IDENTIFIER);
        NAVTEQ.add(RoadSectionCodingSystem.NGX_WAY_IDENTIFIER);
    }

    static
    {
        OSM.add(RoadSectionCodingSystem.OSM_EDGE_IDENTIFIER);
    }

    static
    {
        AUTONAVI.add(RoadSectionCodingSystem.NGX_WAY_IDENTIFIER);
    }

    public static void alias(final String alias, final MapData data)
    {
        mapDataForName.put(alias.toLowerCase(), data);
    }

    public static MapData forName(final String name)
    {
        return ensureNotNull(mapDataForName.get(name));
    }

    private final String name;

    private final transient Set<RoadSectionCodingSystem> codingSystems = new HashSet<>();

    public MapData(final String name)
    {
        this.name = name;
        mapDataForName.add(this);
    }

    public Set<RoadSectionCodingSystem> codingSystems()
    {
        if (this.codingSystems != null)
        {
            if ("TomTom".equalsIgnoreCase(name()))
            {
                this.codingSystems.addAll(TOMTOM.codingSystems);
            }
            else if ("OSM".equalsIgnoreCase(name()))
            {
                this.codingSystems.addAll(OSM.codingSystems);
            }
            else if ("Navteq".equalsIgnoreCase(name()))
            {
                this.codingSystems.addAll(NAVTEQ.codingSystems);
            }
            else if ("AutoNavi".equalsIgnoreCase(name()))
            {
                this.codingSystems.addAll(AUTONAVI.codingSystems);
            }
        }
        return this.codingSystems;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof MapData)
        {
            final var that = (MapData) object;
            return this.name.equals(that.name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    @Override
    public String name()
    {
        return this.name;
    }

    public RoadSectionCodingSystem primaryCodingSystem()
    {
        final var codingSystems = codingSystems();
        if (codingSystems != null)
        {
            final var iterator = codingSystems.iterator();
            if (iterator.hasNext())
            {
                return iterator.next();
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return this.name();
    }

    private void add(final RoadSectionCodingSystem system)
    {
        this.codingSystems.add(system);
    }
}
