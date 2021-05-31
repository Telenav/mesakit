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

package com.telenav.kivakit.graph.specifications.common.shapepoint.store;

import com.telenav.kivakit.collections.primitive.map.scalars.LongToIntMap;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.ShapePoint;
import com.telenav.kivakit.graph.metadata.DataSpecification.GraphElementFactory;
import com.telenav.kivakit.graph.specifications.common.node.store.NodeStore;
import com.telenav.kivakit.map.geography.Location;

import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

public class ShapePointStore extends NodeStore<ShapePoint>
{
    /** A reverse map from shape point identifier back to shape point index */
    @KivaKitArchivedField
    @SuppressWarnings({ "FieldCanBeLocal" })
    private LongToIntMap shapePointIdentifierToShapePointIndex;

    public ShapePointStore(final Graph graph)
    {
        super(graph);
    }

    /**
     * @return Any shape point associated with the given location
     */
    public ShapePoint forLocation(final Location location)
    {
        assert location != null;
        final var identifier = vertexStore().node(location).identifierAsLong();
        if (identifier == -1)
        {
            return null;
        }
        final var point = dataSpecification().newShapePoint(graph(), identifier);
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
    protected GraphElementFactory<ShapePoint> elementFactory()
    {
        return graph().dataSpecification()::newShapePoint;
    }

    @Override
    protected Class<ShapePoint> elementType()
    {
        return ShapePoint.class;
    }
}
