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

package com.telenav.mesakit.map.data.formats.pbf.processing.readers;

import com.telenav.kivakit.core.io.IO;
import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataStatistics;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfStopProcessingException;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfProcessing;
import crosby.binary.osmosis.OsmosisReader;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.util.Map;

/**
 * Reads a PBF file serially by reading from the input resource with osmosis PBF reader and calling {@link
 * #start(PbfDataProcessor)}, {@link #processMetadata(Map)}, {@link #process(Entity)} and {@link #end()}.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
public class SerialPbfReader extends BasePbfReader
{
    public SerialPbfReader(Resource resource)
    {
        super(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PbfDataStatistics onProcess(PbfDataProcessor processor)
    {
        // Start processing
        start(processor);

        // Open the resource for reading
        var in = resource().openForReading();
        try
        {
            // Read the OSM data with this as the sink
            var reader = new OsmosisReader(in);
            reader.setSink(sink());
            reader.run();

            end();
        }
        catch (PbfStopProcessingException ignored)
        {
        }
        finally
        {
            // Close the input
            IO.close(in);
        }

        return dataStatistics();
    }

    /**
     * @return Callback implementation for {@link OsmosisReader}
     */
    private Sink sink()
    {
        var outer = this;
        return new Sink()
        {
            @Override
            public void close()
            {
            }

            @Override
            public void complete()
            {
            }

            @Override

            public void initialize(Map<String, Object> metadata)
            {
                processMetadata(metadata);
            }

            @Override
            public void process(EntityContainer container)
            {
                outer.process(container.getEntity());
            }
        };
    }
}
