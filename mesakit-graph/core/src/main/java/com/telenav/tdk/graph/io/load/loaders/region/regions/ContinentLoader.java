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


package com.telenav.kivakit.graph.io.load.loaders.region.regions;

import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.Place;
import com.telenav.kivakit.graph.collections.EdgeSequence;
import com.telenav.kivakit.graph.io.load.loaders.region.RegionalGraphLoader;
import com.telenav.kivakit.map.region.Continent;

public class ContinentLoader extends RegionalGraphLoader
{
    private final Graph source;

    private final Continent continent;

    public ContinentLoader(final Graph source, final Continent continent)
    {
        this.source = source;
        this.continent = continent;
    }

    @Override
    protected EdgeSequence forwardEdges()
    {
        return this.source.edgesIntersecting(this.continent.bounds(),
                edge -> !edge.isReverse() && ContinentLoader.this.continent.equals(edge.continent()));
    }

    @Override
    protected Iterable<Place> places()
    {
        return this.source.placesInside(this.continent);
    }

    @Override
    protected Graph sourceGraph()
    {
        return this.source;
    }
}
