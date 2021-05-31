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
import com.telenav.kivakit.map.region.State;

import static com.telenav.kivakit.kernel.validation.Validate.fail;

public class StateLoader extends RegionalGraphLoader
{
    private final Graph source;

    private final State state;

    public StateLoader(final Graph source, final State state)
    {
        this.source = source;
        this.state = state;
    }

    @Override
    protected EdgeSequence forwardEdges()
    {
        final var bounds = this.state.bounds();
        if (bounds == null)
        {
            return fail("Bounds for $ is missing", this.state);
        }
        return this.source.edgesIntersecting(this.state.bounds(),
                edge -> !edge.isReverse() && StateLoader.this.state.equals(edge.state()));
    }

    @Override
    protected Iterable<Place> places()
    {
        return this.source.placesInside(this.state);
    }

    @Override
    protected Graph sourceGraph()
    {
        return this.source;
    }
}
