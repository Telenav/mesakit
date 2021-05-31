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

package com.telenav.kivakit.graph.traffic.project;

import com.telenav.kivakit.kernel.language.collections.set.Sets;
import com.telenav.kivakit.kernel.language.io.serialization.KivaKitSerializer;
import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.kernel.project.KivaKitProject;
import com.telenav.kivakit.map.geography.project.KivaKitMapGeography;

import java.util.Set;

public class KivaKitGraphTraffic extends KivaKitProject
{
    private static final Lazy<KivaKitGraphTraffic> singleton = new Lazy<>(KivaKitGraphTraffic::new);

    public static KivaKitGraphTraffic get()
    {
        return singleton.get();
    }

    protected KivaKitGraphTraffic()
    {
    }

    @Override
    public Set<KivaKitProject> dependencies()
    {
        return Sets.of(KivaKitMapGeography.get());
    }

    @Override
    public KivaKitSerializer newSerializer()
    {
        return new KivaKitGraphTrafficKryoSerializer();
    }
}
