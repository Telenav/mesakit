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

package com.telenav.mesakit.graph.specifications.common;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.graph.store.CommonGraphStore;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;

/**
 * Data specification common to all data. Note that common factories, properties and attributes are inherited from
 * {@link DataSpecification}. This is done to allow implementations to only override the parts they want to change.
 *
 * @author jonathanl (shibo)
 */
public class CommonDataSpecification extends DataSpecification
{
    private static final Lazy<CommonDataSpecification> singleton = Lazy.of(CommonDataSpecification::new);

    public static CommonDataSpecification get()
    {
        return singleton.get();
    }

    protected CommonDataSpecification()
    {
    }

    @Override
    public GraphStore newGraphStore(final Graph graph)
    {
        return new CommonGraphStore(graph);
    }

    @Override
    public PlaceStore newPlaceStore(final Graph graph)
    {
        return new PlaceStore(graph);
    }

    @Override
    public RelationStore newRelationStore(final Graph graph)
    {
        return new RelationStore(graph);
    }

    @Override
    public ShapePointStore newShapePointStore(final Graph graph)
    {
        return new ShapePointStore(graph);
    }

    @Override
    public VertexStore newVertexStore(final Graph graph)
    {
        return new VertexStore(graph);
    }

    @Override
    public Type type()
    {
        return Type.Common;
    }

    @Override
    protected Graph onNewGraph(final Metadata metadata)
    {
        return new CommonGraph(metadata);
    }
}
