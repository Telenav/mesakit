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

package com.telenav.mesakit.graph.specifications.common.shapepoint.store;

import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.primitive.collections.map.scalars.LongToIntMap;
import com.telenav.kivakit.resource.compression.archive.ArchivedField;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.ShapePoint;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.node.store.NodeStore;
import com.telenav.mesakit.map.geography.Location;

import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

public class ShapePointStore extends NodeStore<ShapePoint>
{
    /** A reverse map from shape point identifier back to shape point index */
    @ArchivedField
    @SuppressWarnings({ "FieldCanBeLocal" })
    private LongToIntMap shapePointIdentifierToShapePointIndex;

    public ShapePointStore(Graph graph)
    {
        super(graph);
    }

    /**
     * Returns any shape point associated with the given location
     */
    public ShapePoint forLocation(Location location)
    {
        assert location != null;
        var identifier = vertexStore().node(location).identifierAsLong();
        if (identifier == -1)
        {
            return null;
        }
        var point = dataSpecification().newShapePoint(graph(), identifier);
        point.location(location.asLong());
        return point;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimate initialSize()
    {
        return metadata().shapePointCount(ALLOW_ESTIMATE).asEstimate();
    }

    @Override
    public String objectName()
    {
        return "shape-point-store";
    }

    @Override
    public void onInitialize()
    {
        shapePointIdentifierToShapePointIndex = new LongToIntMap(objectName() + ".shapePointIdentifierToShapePointIndex");
        shapePointIdentifierToShapePointIndex.initialSize(metadata().placeCount(ALLOW_ESTIMATE).asEstimate());
        shapePointIdentifierToShapePointIndex.initialize();
    }

    @Override
    protected DataSpecification.GraphElementFactory<ShapePoint> elementFactory()
    {
        return graph().dataSpecification()::newShapePoint;
    }

    @Override
    protected Class<ShapePoint> elementType()
    {
        return ShapePoint.class;
    }
}
