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

package com.telenav.mesakit.graph.io.load.loaders.region.regions;

import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.io.load.loaders.region.RegionalGraphLoader;
import com.telenav.mesakit.map.region.MetropolitanArea;

public class MetropolitanAreaLoader extends RegionalGraphLoader
{
    private final Graph source;

    private final MetropolitanArea metropolitanArea;

    public MetropolitanAreaLoader(final Graph source, final MetropolitanArea metropolitanArea)
    {
        this.source = source;
        this.metropolitanArea = metropolitanArea;
    }

    @Override
    protected EdgeSequence forwardEdges()
    {
        return this.source.edgesIntersecting(this.metropolitanArea.bounds(), edge -> !edge.isReverse()
                && MetropolitanAreaLoader.this.metropolitanArea.equals(edge.metropolitanArea()));
    }

    @Override
    protected Iterable<Place> places()
    {
        return this.source.placesInside(this.metropolitanArea);
    }

    @Override
    protected Graph sourceGraph()
    {
        return this.source;
    }
}
