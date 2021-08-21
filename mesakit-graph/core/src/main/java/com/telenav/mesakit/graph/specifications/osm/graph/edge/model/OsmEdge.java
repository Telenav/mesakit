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

import com.telenav.kivakit.kernel.data.validation.ValidationType;
import com.telenav.kivakit.kernel.data.validation.Validator;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.store.OsmEdgeStore;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineSnapper;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.regions.Country;

public class OsmEdge extends Edge
{
    public OsmEdge(final Graph graph, final EdgeIdentifier identifier)
    {
        super(graph, identifier);
    }

    public OsmEdge(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    public OsmEdge(final Graph graph, final long identifier, final int index)
    {
        super(graph, identifier, index);
    }

    public boolean computeDoubleDigitized(final Angle maximumHeadingDeviation)
    {
        // Don't waste time on edges that can't be double digitized
        if (osmCouldBeDoubleDigitized())
        {
            // Create snapper
            final var snapper = new PolylineSnapper();

            // Go through segments for this edge,
            var doubleDigitized = 0;
            final var segments = roadShape().segments();
            for (final var segment : segments)
            {
                // get the segment mid-point
                final var midpoint = segment.midpoint();

                // and find distinct candidate edges near to the mid-point that could be
                // double-digitized
                for (final var candidate : graph().edgesIntersecting(
                        midpoint.bounds().expanded(roadType().maximumDoubleDigitizationSeparation()),
                        edge -> !equals(edge) && edge.osmCouldBeDoubleDigitized()))
                {
                    // Snap the segment midpoint to the candidate using the segment heading
                    final var snap = snapper.snap(candidate, midpoint, segment.heading());

                    // If the two are close enough
                    if (snap.distanceToSource().isLessThan(roadType().maximumDoubleDigitizationSeparation()))
                    {
                        // and headed in opposite directions
                        if (snap.segmentHeading().isOppositeDirection(segment.heading(), maximumHeadingDeviation))
                        {
                            // and they have the same name
                            if (hasSameRoadNameAs(candidate))
                            {
                                // then the segment is double digitized
                                doubleDigitized++;
                                break;
                            }
                        }
                    }
                }
            }

            // If there are more double-digitized segments than non-double-digitized
            final var nonDoubleDigitized = segments.size() - doubleDigitized;
            if (doubleDigitized > nonDoubleDigitized)
            {
                // then we'll flag it as double-digitized
                return setDoubleDigitized(true);
            }
        }

        // Save double-digitization state
        return setDoubleDigitized(false);
    }

    @Override
    public boolean isIntersectionEdge()
    {
        return isJunctionEdge() || isRoundabout() || ((length().isLessThan(Distance.meters(60)) || isConnector())
                && Route.fromEdge(this).connectsTwoDoubleDigitizedRoads());
    }

    @Override
    public boolean osmCouldBeDoubleDigitized()
    {
        return isOneWay() && isMainRoad();
    }

    /**
     * @return True if this edge is part of a double digitized pair
     */
    @Override
    public Boolean osmIsDoubleDigitized()
    {
        return store().retrieveIsDoubleDigitized(this);
    }

    @Override
    public Distance osmMaximumDoubleDigitizationSeparation()
    {
        // In the US, roads, especially freeways can be quite far apart
        if (Country.UNITED_STATES.equals(country()))
        {
            switch (roadType())
            {
                case FREEWAY:
                    return Distance.meters(1000);

                case HIGHWAY:
                case URBAN_HIGHWAY:
                    return Distance.meters(100);

                case FRONTAGE_ROAD:
                case THROUGHWAY:
                    return Distance.meters(50);

                case LOCAL_ROAD:
                default:
                    return Distance.meters(25);
            }
        }

        // In the EU, things are closer together
        switch (roadType())
        {
            case FREEWAY:
            case HIGHWAY:
            case URBAN_HIGHWAY:
                return Distance.meters(35);

            case FRONTAGE_ROAD:
            case THROUGHWAY:
                return Distance.meters(30);

            case LOCAL_ROAD:
            default:
                return Distance.meters(25);
        }
    }

    @Override
    public EdgeIdentifier rawIdentifier()
    {
        return store().retrieveRawIdentifier(this);
    }

    @Override
    public Validator validator(final ValidationType type)
    {
        return new ElementValidator()
        {
            @Override
            protected void onValidate()
            {
                validate(OsmEdge.super.validator(type));
                glitchIf(laneCount() == null, "laneCount");
            }
        };
    }

    @Override
    @SuppressWarnings({ "exports" })
    protected OsmEdgeStore store()
    {
        return (OsmEdgeStore) graph().edgeStore();
    }

    boolean setDoubleDigitized(final boolean isDoubleDigitized)
    {
        store().storeIsDoubleDigitized(this, isDoubleDigitized);
        return isDoubleDigitized;
    }
}
