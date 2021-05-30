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

package com.telenav.tdk.graph.specifications.unidb.graph.converter;

import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.specifications.common.graph.loader.PbfToGraphConverter;
import com.telenav.tdk.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.tdk.graph.specifications.unidb.graph.loader.UniDbRawPbfGraphLoader;

public class UniDbPbfToGraphConverter extends PbfToGraphConverter
{
    public UniDbPbfToGraphConverter(final Metadata metadata)
    {
        super(metadata);
    }

    @Override
    protected Graph onConvert(final PbfDataSourceFactory input, final Metadata metadata)
    {
        final var loader = listenTo(new UniDbRawPbfGraphLoader(input, metadata, configuration().loaderConfiguration().tagFilter()));
        loader.configure(configuration().loaderConfiguration());

        final var graph = listenTo(metadata.newGraph());

        final var loadedMetaData = graph.load(loader);
        if (loadedMetaData != null)
        {
            graph.metadata(loadedMetaData);
            return graph;
        }
        return null;
    }
}
