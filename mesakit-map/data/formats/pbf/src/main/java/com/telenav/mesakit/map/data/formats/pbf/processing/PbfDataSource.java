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

import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessing;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.core.kernel.language.time.Time;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.kivakit.core.kernel.language.values.mutable.MutableValue;
import com.telenav.kivakit.core.kernel.messaging.Broadcaster;
import com.telenav.kivakit.core.resource.Resourceful;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;

import java.util.Map;

/**
 * A source of PBF data which can be processed by an {@link PbfDataProcessor}. The method {@link
 * #process(PbfDataProcessor)} pushes data to the data processor object, which processes the ways, nodes and relations
 * in receives. The number of ways, nodes and relations in the data source can be retrieved with {@link #ways()}, {@link
 * #nodes()} and {@link #relations()}, respectively. Metadata from the PBF file can be accessed with {@link
 * #metadata()}. The methods {@link #onStart()} are called {@link #onEnd()} are called before and after processing.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
@UmlExcludeSuperTypes(Resourceful.class)
@UmlRelation(label = "calls", referent = PbfDataProcessor.class)
public interface PbfDataSource extends Resourceful, Broadcaster
{
    void expectedNodes(final Count nodes);

    void expectedRelations(final Count relations);

    void expectedWays(final Count ways);

    /**
     * @return The metadata record of this PBF data source
     */
    default Map<String, String> metadata()
    {
        final var value = new MutableValue<Map<String, String>>(null);
        final var reader = new SerialPbfReader(resource());
        reader.silence();
        reader.process(new PbfDataProcessor()
        {
            @Override
            public void onMetadata(final Map<String, String> metadata)
            {
                value.set(metadata);
                throw new PbfStopProcessingException();
            }

            @Override
            public Action onNode(final PbfNode node)
            {
                throw new PbfStopProcessingException();
            }
        });
        return value.get();
    }

    /**
     * @return The number of nodes in this data source
     */
    Count nodes();

    /**
     * Called when processing is over
     */
    void onEnd();

    /**
     * Processes the OSM data from this source using the given data processor
     *
     * @param processor The data processor to call back with ways, nodes and relations as they are read from the data
     * source.
     */
    PbfDataStatistics onProcess(PbfDataProcessor processor);

    /**
     * Called when processing begins
     */
    void onStart();

    /**
     * Specifies a processing phase for annotating the progress of multi-pass operations
     */
    void phase(String phase);

    /**
     * Processes the OSM data from this source using the given data processor
     *
     * @param processor The data processor to call back with ways, nodes and relations as they are read from the data
     * source.
     */
    @UmlRelation(label = "collects")
    default PbfDataStatistics process(final PbfDataProcessor processor)
    {
        final var start = Time.now();
        onStart();
        try
        {
            return onProcess(processor);
        }
        finally
        {
            onEnd();
            information(AsciiArt.bottomLine(30, "Completed processing in $", start.elapsedSince()));
        }
    }

    /**
     * @return The number of relations in this data source
     */
    Count relations();

    /**
     * @return The number of ways in this data source
     */
    Count ways();
}
