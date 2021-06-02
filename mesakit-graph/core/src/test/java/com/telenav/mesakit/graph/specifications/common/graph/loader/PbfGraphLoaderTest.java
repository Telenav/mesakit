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

package com.telenav.mesakit.graph.specifications.common.graph.loader;

/*
 * package com.telenav.tdk.graph.loaders.osm; import static org.junit.assertNotNull; import static
 * org.junit.assertTrue; import org.junit.Test; import com.telenav.mesakit.graph.Graph; import
 * com.telenav.kivakit.graph.OsmEdgeIdentifier; import com.telenav.kivakit.kernel.framework.filesystem.File;
 * import com.telenav.kivakit.kernel.framework.messaging.Listener; import
 * com.telenav.kivakit.data.formats.library.DataSupplier; import
 * com.telenav.kivakit.data.formats.pbf.processing.RelationFilter; import
 * com.telenav.kivakit.data.formats.pbf.WayFilter;
 *//*
 * public class PbfGraphLoaderTest {
 * @Test public void testValidOsmEdgeIdentifier() throws Exception { // A small test pbf file
 * final File pbf = new File(PbfFile.class.resource("pergola_cluj.osm.pbf").getPath());
 * // Load the PBF as a graph final Graph graph = new PbfFile(pbf, DataSupplier.OSM,
 * WayFilter.NAVIGABLE_WAYS, RelationFilter.ALL_RELATIONS) .load(Listener.none()); // Validate the
 * graph loaded assertNotNull(graph); // Go through edges graph.edges().forEach(edge -> {
 * Get from and to node identifiers final long fromNode =
 * edge.osmFromNodeIdentifier().asLong(); final long toNode =
 * edge.osmToNodeIdentifier().asLong(); // Get the Skobbler OSM edge identifier (way:from:to)
 * final OsmEdgeIdentifier osmEdgeIdentifier = edge.osmEdgeIdentifier(); // the from and to
 * should not be equal assertTrue(fromNode != toNode); // and the osm edge identifier's 'from'
 * and 'to' should not be equal assertTrue(osmEdgeIdentifier.getFrom().asLong() !=
 * osmEdgeIdentifier.getTo().asLong()); }); } }
 */
