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

package com.telenav.mesakit.graph.library.osm.change.io;

import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.library.osm.change.store.ModifiedWayStore;
import com.telenav.mesakit.graph.library.osm.change.store.PbfNodeStore;
import com.telenav.mesakit.map.data.formats.pbf.processing.writers.PbfWriter;

public class PbfSaver
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public void save(Graph graph, File output)
    {
        if (graph.supportsFullPbfNodeInformation())
        {
            var start = Time.now();
            var progress = BroadcastingProgressReporter.create(LOGGER);
            var nodes = new PbfNodeStore(graph);
            var ways = new ModifiedWayStore(nodes);
            graph.forwardEdges().stream().forEach(edge ->
            {
                ways.modifiableWay(edge);
                progress.next();
            });
            LOGGER.information("Saving double digitized edges to " + output);
            var writer = new PbfWriter(output, true);
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
