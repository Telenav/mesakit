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

package com.telenav.mesakit.graph.world.project;

import com.telenav.kivakit.kernel.KivaKit;
import com.telenav.kivakit.kernel.language.collections.set.ObjectSet;
import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.project.Project;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.mesakit.graph.project.GraphCoreProject;

public class GraphWorldProject extends Project
{
    private static final Lazy<GraphWorldProject> singleton = Lazy.of(GraphWorldProject::new);

    public static GraphWorldProject get()
    {
        return singleton.get();
    }

    protected GraphWorldProject()
    {
        SerializationSessionFactory.threadLocal(new GraphWorldKryoTypes().sessionFactory());
    }

    @Override
    public ObjectSet<Project> dependencies()
    {
        return ObjectSet.of(GraphCoreProject.get());
    }

    /**
     * NOTE: This version may sometimes be older or newer than the framework version
     *
     * @return The current world graph version.
     */
    public Version worldGraphVersion()
    {
        return KivaKit.get().kivakitVersion();
    }
}
