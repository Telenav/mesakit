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

package com.telenav.tdk.graph.library.osm.change;

import com.telenav.tdk.core.filesystem.File;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.operation.progress.reporters.Progress;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.data.formats.pbf.processing.writers.PbfWriter;
import com.telenav.tdk.graph.Graph;

public class PbfSaver
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public void save(final Graph graph, final File output)
    {
        if (graph.supportsFullPbfNodeInformation())
        {
            final var start = Time.now();
            final var progress = Progress.create(LOGGER);
            final var nodes = new PbfNodeStore(graph);
            final var ways = new ModifiedWayStore(nodes);
            graph.forwardEdges().stream().forEach(edge ->
            {
                ways.modifiableWay(edge);
                progress.next();
            });
            LOGGER.information("Saving double digitized edges to " + output);
            final var writer = new PbfWriter(output, true);
            ways.saveAll(writer);
            writer.close();
            LOGGER.information("Done in $", start.elapsedSince());
        }
        else
        {
            throw new IllegalStateException(
                    "Cannot save to PBF file because graph doesn't contain OSM node information");
        }
    }
}
