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

import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.core.project.ProjectTrait;
import com.telenav.kivakit.core.version.Version;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.primitive.collections.PrimitiveCollectionsKryoTypes;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSessionFactory;
import com.telenav.kivakit.serialization.kryo.types.KivaKitCoreKryoTypes;
import com.telenav.mesakit.core.MesaKit;
import com.telenav.mesakit.map.geography.GeographyKryoTypes;
import com.telenav.mesakit.map.measurements.MeasurementsKryoTypes;

import static com.telenav.kivakit.core.messaging.Listener.throwingListener;

/**
 * This class defines a KivaKit {@link Project}. It cannot be constructed with the new operator since it has a private
 * constructor. To access the singleton instance of this class, call {@link Project#resolveProject(Class)}, or use
 * {@link ProjectTrait#project(Class)}.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SpellCheckingInspection") public class RegionProject extends Project
{
    public RegionProject()
    {
        register(new KryoSerializationSessionFactory(new RegionKryoTypes()
                .mergedWith(new GeographyKryoTypes())
                .mergedWith(new MeasurementsKryoTypes())
                .mergedWith(new PrimitiveCollectionsKryoTypes())
                .mergedWith(new KivaKitCoreKryoTypes())));
    }

    /**
     * Version of border data (the PBF source data)
     */
    public Version borderDataVersion()
    {
        return Version.parseVersion(throwingListener(), "0.9.1");
    }

    /**
     * Returns the folder where various kinds of data are cached
     */
    public Folder mesakitMapFolder()
    {
        return resolveProject(MesaKit.class)
                .mesakitCacheFolder()
                .folder("map")
                .mkdirs();
    }

    @Override
    public void onInitialize()
    {
        Region.bootstrap();
    }
}
