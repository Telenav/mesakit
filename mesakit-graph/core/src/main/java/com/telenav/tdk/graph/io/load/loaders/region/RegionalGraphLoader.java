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

package com.telenav.kivakit.graph.io.load.loaders.region;

import com.telenav.kivakit.kernel.scalars.counts.Count;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.graph.EdgeRelation;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.Metadata;
import com.telenav.kivakit.graph.Place;
import com.telenav.kivakit.graph.collections.EdgeSequence;
import com.telenav.kivakit.graph.io.load.GraphConstraints;
import com.telenav.kivakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.kivakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.kivakit.graph.specifications.library.store.GraphStore;

import java.util.HashSet;
import java.util.Set;

public abstract class RegionalGraphLoader extends BaseGraphLoader
{
    protected RegionalGraphLoader()
    {
    }

    @Override
    public Metadata onLoad(final GraphStore store, final GraphConstraints constraints)
    {
        final var edges = forwardEdges();
        final var relations = relations();
        final var places = places();
        final var edgeCount = store.edgeStore().addAll(edges, constraints);
        store.relationStore().add(relations, constraints);
        add(store.placeStore(), constraints, places);
        return new Metadata().withEdgeCount(edgeCount);
    }

    @Override
    public Resource resource()
    {
        return sourceGraph().resource();
    }

    protected abstract EdgeSequence forwardEdges();

    protected abstract Iterable<Place> places();

    protected Iterable<EdgeRelation> relations()
    {
        final Set<EdgeRelation> relations = new HashSet<>();
        for (final var edge : forwardEdges())
        {
            relations.addAll(edge.relations());
        }
        return relations;
    }

    protected abstract Graph sourceGraph();

    private Count add(final PlaceStore placeStore, final GraphConstraints constraints,
                      final Iterable<? extends Place> places)
    {
        var count = 0;
        final var adder = placeStore.adder();
        for (final Place place : places)
        {
            if (constraints.includes(place))
            {
                adder.add(place);
                count++;
            }
        }
        return Count.of(count);
    }
}
