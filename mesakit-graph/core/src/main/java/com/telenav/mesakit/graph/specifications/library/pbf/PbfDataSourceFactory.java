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

package com.telenav.mesakit.graph.specifications.library.pbf;

import com.telenav.kivakit.interfaces.function.Mapper;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.ParallelPbfReader;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * Creates data sources of a given type to read a resource. The method {@link #map(Metadata)} creates a data
 * source using the provided metadata. This design avoids the need for {@link PbfDataSource} objects to be resettable,
 * which might be error-prone. Instead, data sources can be discarded and a new data source created by this factory.
 *
 * @author jonathanl (shibo)
 */
public class PbfDataSourceFactory extends BaseRepeater implements Mapper<Metadata, PbfDataSource>
{
    public enum Type
    {
        PARALLEL_READER,
        SERIAL_READER
    }

    private final Resource resource;

    private final Type type;

    private final Count threads;

    public PbfDataSourceFactory(Resource resource, Count threads, Type type)
    {
        ensureNotNull(resource);
        ensureNotNull(type);
        ensureNotNull(threads);

        this.resource = resource;
        this.threads = threads;
        this.type = type;
    }

    @Override
    public PbfDataSource map(Metadata metadata)
    {
        switch (type)
        {
            case SERIAL_READER:
            {
                return listenTo(new SerialPbfReader(resource));
            }

            case PARALLEL_READER:
            {
                return listenTo(new ParallelPbfReader(resource, threads));
            }

            default:
                return unsupported("Unsupported data source $", type);
        }
    }

    public Resource resource()
    {
        return resource;
    }
}
