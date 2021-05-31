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

import com.telenav.kivakit.collections.primitive.array.packed.SplitPackedArray;
import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.collections.primitive.map.scalars.LongToLongMap;
import com.telenav.kivakit.kernel.scalars.counts.*;
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
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;

import static com.telenav.kivakit.collections.primitive.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;

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
    private final AttributeReference<LongToLongMap> FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER =
            new AttributeReference<>(this, OsmEdgeAttributes.get().FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER, "forwardTelenavTrafficLocationIdentifier",
                    () -> (LongToLongMap) new LongToLongMap("forwardTelenavTrafficLocationIdentifier")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private LongToLongMap forwardTelenavTrafficLocationIdentifier;

    private final AttributeReference<SplitIntArray> FORWARD_TRACE_COUNT =
            new AttributeReference<>(this, OsmEdgeAttributes.get().FORWARD_TRACE_COUNT, "forwardTraceCount",
                    () -> (SplitIntArray) new SplitIntArray("forwardTraceCount")
                            .nullInt(Integer.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitIntArray forwardTraceCount;

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
                protected void onLoaded(final SplitPackedArray array)
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

    private final AttributeReference<LongToLongMap> REVERSE_TELENAV_TRAFFIC_LOCATION_IDENTIFIER =
            new AttributeReference<>(this, OsmEdgeAttributes.get().REVERSE_TELENAV_TRAFFIC_LOCATION_IDENTIFIER, "reverseTelenavTrafficLocationIdentifier",
                    () -> (LongToLongMap) new LongToLongMap("reverseTelenavTrafficLocationIdentifier")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private LongToLongMap reverseTelenavTrafficLocationIdentifier;

    private final AttributeReference<SplitIntArray> REVERSE_TRACE_COUNT =
            new AttributeReference<>(this, OsmEdgeAttributes.get().FORWARD_TRACE_COUNT, "reverseTraceCount",
                    () -> (SplitIntArray) new SplitIntArray("reverseTraceCount")
                            .nullInt(Integer.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitIntArray reverseTraceCount;

    public OsmEdgeStore(final Graph graph)
    {
        super(graph);
    }

    public final RoadSectionIdentifier retrieveForwardTelenavTrafficLocationIdentifier(final Edge edge)
    {
        FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER.load();
        if (forwardTelenavTrafficLocationIdentifier != null)
        {
            final var ttl = forwardTelenavTrafficLocationIdentifier.get(edge.identifierAsLong());
            if (!forwardTelenavTrafficLocationIdentifier.isNull(ttl))
            {
                return RoadSectionIdentifier.forCodingSystemAndIdentifier(RoadSectionCodingSystem.TELENAV_TRAFFIC_LOCATION, ttl, false);
            }
        }
        return null;
    }

    public final boolean retrieveIsDoubleDigitized(final Edge edge)
    {
        return IS_DOUBLE_DIGITIZED.retrieveBoolean(edge);
    }

    public final EdgeIdentifier retrieveRawIdentifier(final Edge edge)
    {
        return RAW_IDENTIFIER.retrieveObject(edge, EdgeIdentifier::new);
    }

    public final RoadSectionIdentifier retrieveReverseTelenavTrafficLocationIdentifier(final Edge edge)
    {
        REVERSE_TELENAV_TRAFFIC_LOCATION_IDENTIFIER.load();
        if (reverseTelenavTrafficLocationIdentifier != null)
        {
            final var ttl = reverseTelenavTrafficLocationIdentifier.get(edge.identifierAsLong());
            if (!reverseTelenavTrafficLocationIdentifier.isNull(ttl))
            {
                return RoadSectionIdentifier.forCodingSystemAndIdentifier(RoadSectionCodingSystem.TELENAV_TRAFFIC_LOCATION, ttl, false);
            }
        }
        return null;
    }

    public Count retrieveTraceCount(final Edge edge)
    {
        FORWARD_TRACE_COUNT.load();
        REVERSE_TRACE_COUNT.load();
        if (edge.isReverse())
        {
            if (reverseTraceCount != null)
            {
                final var count = reverseTraceCount.safeGet(edge.index());
                if (!reverseTraceCount.isNull(count))
                {
                    return Count.count(count);
                }
            }
        }
        else
        {
            if (forwardTraceCount != null)
            {
                final var count = forwardTraceCount.get(edge.index());
                if (!forwardTraceCount.isNull(count))
                {
                    return Count.count(count);
                }
            }
        }
        return null;
    }

    /**
     * Stores all of the simple attributes of the given edge at the given edge index
     */

    @Override
    public void storeAttributes(final Edge uncast)
    {
        super.storeAttributes(uncast);

        final var edge = (OsmHeavyWeightEdge) uncast;

        // Store attributes
        RAW_IDENTIFIER.storeObject(edge, edge.rawIdentifier());
        storeTelenavTrafficLocationIdentifiers(edge);
        if (edge.osmTraceCount() != null)
        {
            storeTraceCount(edge, edge.osmTraceCount());
        }
        if (edge.osmIsDoubleDigitized() != null)
        {
            storeIsDoubleDigitized(edge, edge.osmIsDoubleDigitized());
        }

        // If the edge is known to be double digitized
        final var isKnownDoubleDigitized = edge.isKnownDoubleDigitized();
        if (isKnownDoubleDigitized != null)
        {
            // then we can store that state
            storeIsDoubleDigitized(edge, isKnownDoubleDigitized);
        }
    }

    public final void storeIsDoubleDigitized(final Edge edge, final boolean isDoubleDigitized)
    {
        IS_DOUBLE_DIGITIZED.storeBoolean(edge, isDoubleDigitized);
    }

    public final void storeTraceCount(final Edge edge, final Count count)
    {
        if (count != null)
        {
            if (forwardTraceCount == null)
            {
                FORWARD_TRACE_COUNT.allocate();
                REVERSE_TRACE_COUNT.allocate();
            }
            if (edge.isReverse())
            {
                reverseTraceCount.set(edge.index(), count.asInt());
            }
            else
            {
                forwardTraceCount.set(edge.index(), count.asInt());
            }
        }
    }

    private OsmEdgeStore outer()
    {
        return this;
    }

    private void storeTelenavTrafficLocationIdentifiers(final Edge edge)
    {
        // Get normal forward TTL
        var forward = edge.osmTelenavTrafficLocationIdentifier();

        // If there's no forward TTL but the edge is one-way
        if (forward == null && edge.isOneWay())
        {
            // then get the reverse TTL in case the edge is backwards
            final var reversed = edge.reversed();
            if (reversed != null)
            {
                forward = reversed.osmTelenavTrafficLocationIdentifier();
            }
        }

        // If we have a forward TTL
        if (forward != null)
        {
            // store it under the forward identifier
            FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER.allocate();
            forwardTelenavTrafficLocationIdentifier.put(edge.identifierAsLong(), forward.value().asLong());
        }

        // If the edge is two way
        if (edge.isTwoWay())
        {
            // reverse the edge
            final var reversed = edge.reversed();

            // to get the reverse TTL
            final var reverseIdentifier = reversed.osmTelenavTrafficLocationIdentifier();
            if (reverseIdentifier != null)
            {
                // and store it under the reverse identifier
                REVERSE_TELENAV_TRAFFIC_LOCATION_IDENTIFIER.allocate();
                reverseTelenavTrafficLocationIdentifier.put(reversed.identifierAsLong(), reverseIdentifier.value().asLong());
            }
        }
    }
}
