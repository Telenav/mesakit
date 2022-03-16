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

package com.telenav.mesakit.graph.specifications.common.edge.store.index;

import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.primitive.collections.CompressibleCollection;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.specifications.common.vertex.store.EdgeArrayStore;

import java.util.AbstractList;
import java.util.List;

/**
 * Stores lists of edges given an index and a list of edges. The edges can later be retrieved by passing the index to
 * {@link #get(int)}.
 *
 * @author jonathanl (shibo)
 */
public class CompressedEdgeListStore implements CompressibleCollection, NamedObject
{
    /** Store of edges */
    private EdgeArrayStore store;

    /** The graph being accessed */
    private transient Graph graph;

    /** The name of this object */
    private String objectName;

    /**
     * Construct an edge list store for the given graph
     */
    public CompressedEdgeListStore(String objectName, Graph graph)
    {
        this.graph = graph;

        this.objectName = objectName;
        store = new EdgeArrayStore(objectName + ".store", graph.metadata());
    }

    protected CompressedEdgeListStore()
    {
    }

    public int add(List<Edge> edges)
    {
        return store.list(edges);
    }

    @Override
    public Method compress(Method method)
    {
        return store.compress(method);
    }

    @Override
    public CompressibleCollection.Method compressionMethod()
    {
        return store.compressionMethod();
    }

    /**
     * @return The edge list for the given index
     */
    public List<Edge> get(int list)
    {
        var edges = store.list(list);
        var outer = this;
        return new AbstractList<>()
        {
            @Override
            public Edge get(int index)
            {
                return outer.graph.edgeStore().edgeForIndex(edges.get(index));
            }

            @Override
            public int size()
            {
                return outer.store.size(list);
            }
        };
    }

    public void graph(Graph graph)
    {
        this.graph = graph;
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    public int size(int list)
    {
        return store.size(list);
    }
}
