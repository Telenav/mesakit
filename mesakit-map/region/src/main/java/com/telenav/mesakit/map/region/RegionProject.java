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

package com.telenav.mesakit.map.region;

import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.language.version.Version;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.coreproject.Project;
import com.telenav.kivakit.primitive.collections.project.PrimitiveCollectionsKryoTypes;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.kivakit.serialization.kryo.CoreKernelKryoTypes;
import com.telenav.kivakit.serialization.kryo.KryoTypes;
import com.telenav.mesakit.core.MesaKit;
import com.telenav.mesakit.map.geography.project.GeographyKryoTypes;
import com.telenav.mesakit.map.measurements.project.MeasurementsKryoTypes;
import com.telenav.mesakit.map.region.project.RegionKryoTypes;

public class RegionProject extends Project
{
    private static final KryoTypes KRYO_TYPES = new RegionKryoTypes()
            .mergedWith(new GeographyKryoTypes())
            .mergedWith(new MeasurementsKryoTypes())
            .mergedWith(new PrimitiveCollectionsKryoTypes())
            .mergedWith(new CoreKernelKryoTypes());

    private static final Lazy<RegionProject> project = Lazy.of(RegionProject::new);

    public static RegionProject get()
    {
        return project.get();
    }

    protected RegionProject()
    {
        SerializationSessionFactory.threadLocal(KRYO_TYPES.sessionFactory());
    }

    /**
     * Version of border data (the PBF source data)
     */
    public Version borderDataVersion()
    {
        return Version.parse(Listener.console(), "0.9.1");
    }

    /**
     * @return The folder where various kinds of data are cached
     */
    public Folder mesakitMapFolder()
    {
        return MesaKit.get().mesakitCacheFolder()
                .folder("map")
                .mkdirs();
    }

    @Override
    public void onInitialize()
    {
        super.onInitialize();

        Region.bootstrap();
    }
}
