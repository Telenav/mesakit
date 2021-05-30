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

package com.telenav.tdk.graph.specifications.unidb.graph.loader;

import com.telenav.tdk.graph.Metadata;
import com.telenav.tdk.graph.io.load.GraphConstraints;
import com.telenav.tdk.graph.specifications.common.graph.loader.PbfGraphLoader;
import com.telenav.tdk.graph.specifications.library.store.GraphStore;
import com.telenav.tdk.graph.specifications.unidb.UniDbDataSpecification;

import static com.telenav.tdk.graph.Metadata.CountType.REQUIRE_EXACT;

/**
 * The {@link UniDbPbfGraphLoader} loads data meeting the {@link UniDbDataSpecification} from PBF format into a {@link
 * GraphStore}.
 *
 * @author jonathanl (shibo)
 * @see PbfGraphLoader
 */
public class UniDbPbfGraphLoader extends PbfGraphLoader
{
    @Override
    public Metadata onLoad(final GraphStore store, final GraphConstraints constraints)
    {
        // Create and configure a raw graph loader,
        final var loader = new UniDbRawPbfGraphLoader(dataSourceFactory(), metadata(), configuration().tagFilter());
        loader.broadcastTo(this);
        loader.configure(configuration());

        // then load the raw graph directly (since there is no way sectioning with UniDb data)
        final var metadata = store.graph().load(loader, constraints);
        if (metadata != null)
        {
            information(phase() + "Loaded $ edges", metadata.edgeCount(REQUIRE_EXACT));
        }
        else
        {
            problem("Unable to load raw graph");
        }
        return metadata;
    }
}
