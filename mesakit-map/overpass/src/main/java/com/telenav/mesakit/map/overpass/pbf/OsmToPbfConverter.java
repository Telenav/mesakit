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

package com.telenav.mesakit.map.overpass.pbf;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.overpass.project.lexakai.diagrams.DiagramOverpass;
import crosby.binary.osmosis.OsmosisSerializer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.util.Map;

/**
 * <b>Not public API</b>
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramOverpass.class)
public class OsmToPbfConverter extends BaseRepeater
{
    public OsmToPbfConverter(Listener listener)
    {
        addListener(listener);
    }

    public void convert(File in, File out)
    {
        try
        {
            var output = new BlockOutputStream(out.openForWriting());
            var writer = new OsmosisSerializer(output);
            var readerSink = new Sink()
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
                }

                @Override
                public void process(EntityContainer entityContainer)
                {
                    writer.process(entityContainer);
                }
            };

            RunnableSource reader = new XmlReader(in.asJavaFile(), false, CompressionMethod.None);
            reader.setSink(readerSink);
            reader.run();
            writer.complete();
            writer.close();
        }
        catch (Exception e)
        {
            problem(e, "Unable to convert $ to $", in, out);
        }
    }
}
