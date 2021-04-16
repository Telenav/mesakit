////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.processing;

import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessing;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;

import java.util.Collection;
import java.util.Map;

import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;

/**
 * Interface to code that can process OSM data read from an {@link PbfDataSource}. Implementations can handle nodes,
 * ways and relations as they are read and can also do things before and after each type of data (note that data is
 * always read in the following order from an {@link PbfDataSource}: nodes, ways, relations).
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
@UmlRelation(label = "guided by", referent = PbfDataProcessor.Action.class)
public interface PbfDataProcessor
{
    @UmlClassDiagram(diagram = DiagramPbfProcessing.class)
    enum Action
    {
        ACCEPTED,
        DISCARDED,
        FILTERED_OUT
    }

    default void onBounds(final Bound bounds)
    {
    }

    default void onEndNodes()
    {
    }

    default void onEndRelations()
    {
    }

    default void onEndWays()
    {
    }

    /**
     * Called for all ways, nodes and relations (but not for any other entities)
     */
    default void onEntity(final PbfEntity<?> entity)
    {
    }

    default void onMetadata(final Map<String, String> metadata)
    {
    }

    default Action onNode(final PbfNode node)
    {
        return ACCEPTED;
    }

    default void onNodes(final Collection<PbfNode> nodes)
    {
        for (final var node : nodes)
        {
            onNode(node);
        }
    }

    default Action onRelation(final PbfRelation relation)
    {
        return ACCEPTED;
    }

    default void onRelations(final Collection<PbfRelation> relations)
    {
        for (final var relation : relations)
        {
            onRelation(relation);
        }
    }

    @SuppressWarnings("EmptyMethod")
    default void onStartNodes()
    {
    }

    @SuppressWarnings("EmptyMethod")
    default void onStartRelations()
    {
    }

    @SuppressWarnings("EmptyMethod")
    default void onStartWays()
    {
    }

    default Action onWay(final PbfWay way)
    {
        return ACCEPTED;
    }

    default void onWays(final Collection<PbfWay> ways)
    {
        for (final var way : ways)
        {
            onWay(way);
        }
    }
}
