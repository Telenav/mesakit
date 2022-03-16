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

package com.telenav.mesakit.graph.specifications.osm.graph.edge.store;

import com.telenav.kivakit.core.value.count.BitCount;
import com.telenav.kivakit.primitive.collections.array.packed.SplitPackedArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitLongArray;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.graph.specifications.osm.graph.OsmGraph;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.OsmHeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeAttributes;

import static com.telenav.kivakit.primitive.collections.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;

/**
 * Store of edge attributes that are specific to the {@link OsmDataSpecification}.
 *
 * @author jonathanl (shibo)
 * @see EdgeAttributes
 * @see EdgeStore
 * @see ArchivedGraphElementStore
 */
@SuppressWarnings({ "unused" })
public final class OsmEdgeStore extends EdgeStore
{
    @KivaKitArchivedField
    private SplitPackedArray isDoubleDigitized;

    private final AttributeReference<SplitPackedArray> IS_DOUBLE_DIGITIZED =
            new AttributeReference<>(this, OsmEdgeAttributes.get().IS_DOUBLE_DIGITIZED, "isDoubleDigitized",
                    () -> (SplitPackedArray) new SplitPackedArray("isDoubleDigitized")
                            .bits(BitCount._1, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()))
            {
                @Override
                protected void onLoaded(SplitPackedArray array)
                {
                    super.onLoaded(array);

                    // If we don't have any double digitized state yet,
                    if (outer().isDoubleDigitized == null)
                    {
                        // we can compute double digitized edges on-the-fly
                        ((OsmGraph) graph()).markDoubleDigitizedEdges();
                    }
                }
            };

    private final AttributeReference<SplitLongArray> RAW_IDENTIFIER =
            new AttributeReference<>(this, OsmEdgeAttributes.get().RAW_IDENTIFIER, "rawIdentifier",
                    () -> (SplitLongArray) new SplitLongArray("rawIdentifier")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitLongArray rawIdentifier;

    public OsmEdgeStore(Graph graph)
    {
        super(graph);
    }

    public boolean retrieveIsDoubleDigitized(Edge edge)
    {
        return IS_DOUBLE_DIGITIZED.retrieveBoolean(edge);
    }

    public EdgeIdentifier retrieveRawIdentifier(Edge edge)
    {
        return RAW_IDENTIFIER.retrieveObject(edge, EdgeIdentifier::new);
    }

    /**
     * Stores all of the simple attributes of the given edge at the given edge index
     */

    @Override
    public void storeAttributes(Edge uncast)
    {
        super.storeAttributes(uncast);

        var edge = (OsmHeavyWeightEdge) uncast;

        // Store attributes
        RAW_IDENTIFIER.storeObject(edge, edge.rawIdentifier());
        if (edge.osmIsDoubleDigitized() != null)
        {
            storeIsDoubleDigitized(edge, edge.osmIsDoubleDigitized());
        }

        // If the edge is known to be double digitized
        var isKnownDoubleDigitized = edge.isKnownDoubleDigitized();
        if (isKnownDoubleDigitized != null)
        {
            // then we can store that state
            storeIsDoubleDigitized(edge, isKnownDoubleDigitized);
        }
    }

    public void storeIsDoubleDigitized(Edge edge, boolean isDoubleDigitized)
    {
        IS_DOUBLE_DIGITIZED.storeBoolean(edge, isDoubleDigitized);
    }

    private OsmEdgeStore outer()
    {
        return this;
    }
}
