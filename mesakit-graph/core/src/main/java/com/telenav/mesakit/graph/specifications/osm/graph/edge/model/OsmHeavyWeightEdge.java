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

package com.telenav.mesakit.graph.specifications.osm.graph.edge.model;

import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeAttributes;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;

import java.util.Objects;

public class OsmHeavyWeightEdge extends HeavyWeightEdge
{
    private RoadSectionIdentifier telenavTrafficLocationIdentifier;

    private RoadSectionIdentifier reverseTelenavTrafficLocationIdentifier;

    private Boolean isKnownDoubleDigitized;

    private Count traceCount;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public OsmHeavyWeightEdge(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public OsmHeavyWeightEdge(final HeavyWeightEdge that)
    {
        super(that);
        copy(that);
    }

    @Override
    public void copy(final Edge that)
    {
        super.copy(that);

        traceCount(that.osmTraceCount());

        if (that.supports(OsmEdgeAttributes.get().FORWARD_TELENAV_TRAFFIC_LOCATION_IDENTIFIER))
        {
            osmTelenavTrafficLocationIdentifier(that.osmTelenavTrafficLocationIdentifier());
            if (that.isTwoWay())
            {
                osmReverseTelenavTrafficLocationIdentifier(that.reversed().osmTelenavTrafficLocationIdentifier());
            }
        }
    }

    public Boolean isKnownDoubleDigitized()
    {
        return Objects.requireNonNullElse(isKnownDoubleDigitized, false);
    }

    public void knownDoubleDigitized(final Boolean isDoubleDigitized)
    {
        isKnownDoubleDigitized = isDoubleDigitized;
    }

    public void osmReverseTelenavTrafficLocationIdentifier(
            final RoadSectionIdentifier reverseTelenavTrafficLocationIdentifier)
    {
        this.reverseTelenavTrafficLocationIdentifier = reverseTelenavTrafficLocationIdentifier;
    }

    @Override
    public RoadSectionIdentifier osmTelenavTrafficLocationIdentifier()
    {
        if (isForward())
        {
            return telenavTrafficLocationIdentifier;
        }
        else
        {
            return reverseTelenavTrafficLocationIdentifier;
        }
    }

    public void osmTelenavTrafficLocationIdentifier(final RoadSectionIdentifier telenavTrafficLocationIdentifier)
    {
        this.telenavTrafficLocationIdentifier = telenavTrafficLocationIdentifier;
    }

    @Override
    public Count osmTraceCount()
    {
        return traceCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populateWithTestValues()
    {
        super.populateWithTestValues();
        knownDoubleDigitized(false);
    }

    @Override
    public OsmHeavyWeightEdge reversed()
    {
        final var reversed = (OsmHeavyWeightEdge) super.reversed();
        reversed.osmTelenavTrafficLocationIdentifier(osmReverseTelenavTrafficLocationIdentifier());
        reversed.osmReverseTelenavTrafficLocationIdentifier(osmTelenavTrafficLocationIdentifier());
        return reversed;
    }

    @Override
    public void traceCount(final Count traceCount)
    {
        this.traceCount = traceCount;
    }

    private RoadSectionIdentifier osmReverseTelenavTrafficLocationIdentifier()
    {
        return reverseTelenavTrafficLocationIdentifier;
    }
}
