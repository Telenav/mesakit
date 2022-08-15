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

import com.telenav.kivakit.core.collections.set.BaseSet;
import com.telenav.kivakit.core.string.Join;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegion;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

@SuppressWarnings("rawtypes")
@UmlClassDiagram(diagram = DiagramRegion.class)
@UmlExcludeSuperTypes
@UmlRelation(label = "contains", referent = Region.class, referentCardinality = "*")
public class RegionSet extends BaseSet<Region>
{
    public RegionSet()
    {
        super(new LinkedHashSet<>());
    }

    public RegionSet(Iterable<? extends Region> regions)
    {
        super(new LinkedHashSet<>());
        addAll(regions);
    }

    public RegionSet(Set<Region> set)
    {
        super(set);
    }

    @Override
    public boolean add(Region region)
    {
        ensureNotNull(region);
        return super.add(region);
    }

    @Override
    public Count count()
    {
        return Count.count(this);
    }

    @Override
    public BaseSet<Region> onNewInstance()
    {
        return new RegionSet();
    }

    @Override
    public String toString()
    {
        return Join.join(this, ", ");
    }

    @SuppressWarnings("unchecked")
    public RegionSet under()
    {
        var under = new RegionSet();
        for (var region : this)
        {
            under.add(region);
            under.addAll(region.nestedChildren());
        }
        return under;
    }
}
