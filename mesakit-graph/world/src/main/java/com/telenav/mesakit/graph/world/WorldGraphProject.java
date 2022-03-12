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

package com.telenav.mesakit.graph.world;

import com.telenav.kivakit.core.collections.set.ObjectSet;
import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.core.project.ProjectTrait;
import com.telenav.kivakit.core.version.Version;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSessionFactory;
import com.telenav.mesakit.graph.GraphProject;
import com.telenav.mesakit.graph.world.project.WorldGraphKryoTypes;

/**
 * This class defines a KivaKit {@link Project}. It cannot be constructed with the new operator since it has a private
 * constructor. To access the singleton instance of this class, call {@link Project#resolveProject(Class)}, or use
 * {@link ProjectTrait#project(Class)}.
 *
 * @author jonathanl (shibo)
 */
public class WorldGraphProject extends Project
{
    public WorldGraphProject()
    {
        register(new KryoSerializationSessionFactory(new WorldGraphKryoTypes()));
    }

    @Override
    public ObjectSet<Project> dependencies()
    {
        return ObjectSet.objectSet(project(GraphProject.class));
    }

    /**
     * NOTE: This version may sometimes be older or newer than the framework version
     *
     * @return The current world graph version.
     */
    public Version worldGraphVersion()
    {
        return kivakit().kivakitVersion();
    }
}
