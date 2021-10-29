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

import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;

import java.util.Objects;

public class OsmHeavyWeightEdge extends HeavyWeightEdge
{
    private Boolean isKnownDoubleDigitized;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public OsmHeavyWeightEdge(Graph graph, long identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public OsmHeavyWeightEdge(HeavyWeightEdge that)
    {
        super(that);
        copy(that);
    }

    public Boolean isKnownDoubleDigitized()
    {
        return Objects.requireNonNullElse(isKnownDoubleDigitized, false);
    }

    public void knownDoubleDigitized(Boolean isDoubleDigitized)
    {
        isKnownDoubleDigitized = isDoubleDigitized;
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
        return (OsmHeavyWeightEdge) super.reversed();
    }
}
