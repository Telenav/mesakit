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


package com.telenav.tdk.graph.io.load.loaders.region.regions;

import com.telenav.tdk.graph.Graph;
import com.telenav.tdk.graph.Place;
import com.telenav.tdk.graph.collections.EdgeSequence;
import com.telenav.tdk.graph.io.load.loaders.region.RegionalGraphLoader;
import com.telenav.tdk.map.region.Country;

public class CountryLoader extends RegionalGraphLoader
{
    private final Graph source;

    private final Country country;

    public CountryLoader(final Graph source, final Country country)
    {
        this.source = source;
        this.country = country;
    }

    @Override
    protected EdgeSequence forwardEdges()
    {
        return this.source.edgesIntersecting(this.country.bounds(),
                edge -> !edge.isReverse() && CountryLoader.this.country.equals(edge.country()));
    }

    @Override
    protected Iterable<Place> places()
    {
        return this.source.placesInside(this.country);
    }

    @Override
    protected Graph sourceGraph()
    {
        return this.source;
    }
}
