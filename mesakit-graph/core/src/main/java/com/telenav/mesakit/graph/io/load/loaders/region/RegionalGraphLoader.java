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

package com.telenav.mesakit.graph.io.load.loaders.region;

import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;

import java.util.HashSet;
import java.util.Set;

public abstract class RegionalGraphLoader extends BaseGraphLoader
{
    protected RegionalGraphLoader()
    {
    }

    @Override
    public Metadata onLoad(GraphStore store, GraphConstraints constraints)
    {
        var edges = forwardEdges();
        var relations = relations();
        var places = places();
        var edgeCount = store.edgeStore().addAll(edges, constraints);
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
        Set<EdgeRelation> relations = new HashSet<>();
        for (var edge : forwardEdges())
        {
            relations.addAll(edge.relations());
        }
        return relations;
    }

    protected abstract Graph sourceGraph();

    private Count add(PlaceStore placeStore, GraphConstraints constraints,
                      Iterable<? extends Place> places)
    {
        var count = 0;
        var adder = placeStore.adder();
        for (Place place : places)
        {
            if (constraints.includes(place))
            {
                adder.add(place);
                count++;
            }
        }
        return Count.count(count);
    }
}
