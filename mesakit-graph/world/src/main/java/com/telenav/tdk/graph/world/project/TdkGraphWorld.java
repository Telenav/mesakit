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

package com.telenav.kivakit.graph.world.project;

import com.telenav.kivakit.kernel.language.collections.set.Sets;
import com.telenav.kivakit.kernel.language.io.serialization.KivaKitSerializer;
import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.kernel.project.KivaKit;
import com.telenav.kivakit.kernel.project.KivaKitProject;
import com.telenav.kivakit.kernel.scalars.versioning.Version;
import com.telenav.kivakit.graph.project.KivaKitGraphCore;

import java.util.Set;

public class KivaKitGraphWorld extends KivaKitProject
{
    private static final Lazy<KivaKitGraphWorld> singleton = new Lazy<>(KivaKitGraphWorld::new);

    public static KivaKitGraphWorld get()
    {
        return singleton.get();
    }

    protected KivaKitGraphWorld()
    {
    }

    @Override
    public Set<KivaKitProject> dependencies()
    {
        return Sets.of(KivaKitGraphCore.get());
    }

    @Override
    public KivaKitSerializer newSerializer()
    {
        return new KivaKitGraphWorldKryoSerializer();
    }

    /**
     * NOTE: This version may sometimes be older or newer than the TDK version
     *
     * @return The current world graph version.
     */
    public Version worldGraphVersion()
    {
        return KivaKit.version();
    }
}
