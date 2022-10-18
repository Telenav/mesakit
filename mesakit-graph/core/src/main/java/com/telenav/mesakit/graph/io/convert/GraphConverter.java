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

package com.telenav.mesakit.graph.io.convert;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.core.messaging.Repeater;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfToGraphConverter;
import com.telenav.mesakit.graph.specifications.osm.graph.converter.OsmPbfToGraphConverter;

/**
 * A graph converter converts some kind of input {@link File} to a {@link Graph}.
 * <p>
 * The subclass {@link PbfToGraphConverter} implements conversion of PBF graph resources to Graph form and the {@link
 * OsmPbfToGraphConverter} subclass of that class convert PBF data from the OSM specification.
 *
 * @author jonathanl (shibo)
 * @see Graph
 * @see PbfToGraphConverter
 * @see OsmPbfToGraphConverter
 */
public interface GraphConverter extends Repeater
{
    /**
     * Returns a graph constructed from data in the given file
     */
    Graph convert(File input);
}
