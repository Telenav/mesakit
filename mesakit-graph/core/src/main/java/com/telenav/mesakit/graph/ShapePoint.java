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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.kivakit.core.language.strings.conversion.AsIndentedString;
import com.telenav.mesakit.graph.identifiers.ShapePointIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import static com.telenav.kivakit.ensure.Ensure.unsupported;

public class ShapePoint extends GraphNode
{
    /** The location of the shape point */
    private long location;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public ShapePoint(Graph graph, long identifier)
    {
        graph(graph);
        identifier(identifier);
    }

    @Override
    public GraphElement asHeavyWeight()
    {
        return unsupported();
    }

    @Override
    public GraphElementAttributes<?> attributes()
    {
        return unsupported();
    }

    @Override
    public Rectangle bounds()
    {
        return location().bounds();
    }

    @Override
    public ShapePointIdentifier identifier()
    {
        return new ShapePointIdentifier(identifierAsLong());
    }

    /**
     * @return The location of this shape point
     */
    @Override
    public Location location()
    {
        return Location.dm7(location);
    }

    public ShapePoint location(long location)
    {
        this.location = location;
        return this;
    }

    @Override
    public MapNodeIdentifier mapIdentifier()
    {
        return store().retrieveNodeIdentifier(this);
    }

    /**
     * @return The properties of this element from its {@link DataSpecification},
     * @see GraphElementPropertySet
     * @see Stringable
     * @see AsIndentedString
     */
    @Override
    public GraphElementPropertySet<ShapePoint> properties()
    {
        return dataSpecification().shapePointProperties();
    }

    /**
     * @return The graph store for this shape point
     */
    @Override
    public final ShapePointStore store()
    {
        return graph().shapePointStore();
    }
}
