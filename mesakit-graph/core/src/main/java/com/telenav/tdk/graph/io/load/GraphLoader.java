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

package com.telenav.kivakit.graph.io.load;

import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.messaging.Repeater;
import com.telenav.kivakit.kernel.validation.Validation;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.Metadata;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.specifications.common.graph.loader.RawPbfGraphLoader;
import com.telenav.kivakit.graph.specifications.library.store.GraphStore;
import com.telenav.kivakit.graph.specifications.osm.graph.loader.sectioner.WaySectioningGraphLoader;

/**
 * A graph loader takes data from some data source and loads it into a {@link GraphStore}, applying a set of constraints
 * that potentially limits which graph elements are loaded and which are not. Graph loaders are only responsible for
 * reading data and adding it to the provided {@link GraphStore}. Details about how the {@link Graph} and {@link
 * GraphStore} manage the loaded details are private to those objects.
 * <p>
 * Graph loaders are implementation details for loading {@link Graph} objects from various data sources. If you need to
 * load a {@link GraphArchive}, use {@link GraphArchive#load(Listener)}. If  you need to load some other kind of graph
 * resource, use the {@link SmartGraphLoader}, which will load graphs from any supported format:
 * <pre>
 *     private static final Logger LOGGER = LoggerFactory.newLogger();
 *       [...]
 *     var resource = new File(...);
 *     var graph = new SmartGraphLoader(resource).load(LOGGER);
 * </pre>
 *
 * @author jonathanl (shibo)
 * @see Graph#load(GraphLoader, GraphConstraints)
 */
public interface GraphLoader extends Repeater<Message>
{
    /**
     * Called when loading is complete to allow finalization of the loaded data. For example, this is where OSM  grade
     * separations are added based on the final OSM graph.
     *
     * @param store The graph into which data has been loaded
     */
    default void onCommit(final GraphStore store)
    {
    }

    /**
     * Called to load edge and vertex data into the given graph from some storage medium.
     *
     * @param store The graph store to load data into
     * @param constraints The constraints restricting what graph elements should be added to the graph
     * @return Metadata describing the data that was loaded
     */
    Metadata onLoad(final GraphStore store, final GraphConstraints constraints);

    /**
     * @return The resource from which loaded data originates. In the case of a database or some similar source that is
     * not a {@link Resource}, the value returned may be null.
     */
    Resource resource();

    /**
     * @return The kind of validation that should be performed on the data loaded by this graph loader. Many graph
     * loaders validate all data with {@link Validation#VALIDATE_ALL} but some don't load complete data. An example of
     * an incomplete graph loader is {@link RawPbfGraphLoader}, which does not load vertexes or relations. Another is
     * {@link WaySectioningGraphLoader}, is incomplete because it does not load relations. These graph loaders may
     * specify another kind of validation.
     */
    default Validation validation()
    {
        return Validation.VALIDATE_ALL;
    }
}
