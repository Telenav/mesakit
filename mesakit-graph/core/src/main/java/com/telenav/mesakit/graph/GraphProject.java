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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.core.collections.set.ObjectSet;
import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.core.project.ProjectTrait;
import com.telenav.kivakit.core.vm.JavaVirtualMachine;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.kivakit.serialization.kryo.KryoObjectSerializer;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSessionFactory;
import com.telenav.mesakit.core.MesaKit;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.PbfFilters;
import com.telenav.mesakit.map.region.RegionProject;

import static com.telenav.kivakit.core.collections.set.ObjectSet.objectSet;

/**
 * This class defines a KivaKit {@link Project}. It cannot be constructed with the new operator since it has a private
 * constructor. To access the singleton instance of this class, call {@link Project#resolveProject(Class)}, or use
 * {@link ProjectTrait#project(Class)}.
 *
 * @author jonathanl (shibo)
 */
public class GraphProject extends Project
{
    public GraphProject()
    {
        System.setProperty("mesakit.graph.folder", graphFolder().toString());
        JavaVirtualMachine.javaVirtualMachine().invalidateProperties();

        register(new KryoObjectSerializer(new GraphKryoTypes()));
    }

    @Override
    public ObjectSet<Class<? extends Project>> dependencies()
    {
        return objectSet(RegionProject.class);
    }

    /**
     * @return The graph folder where various kinds of data are cached
     */
    public Folder graphFolder()
    {
        return resolveProject(MesaKit.class).mesakitCacheFolder()
                .folder("graph")
                .mkdirs();
    }

    @Override
    public void onInitialize()
    {
        PbfFilters.loadAll();

        super.onInitialize();
    }

    public Folder overpassFolder()
    {
        return graphFolder().folder("overpass").mkdirs();
    }

    public SerializationSessionFactory serializationFactory()
    {
        return new KryoSerializationSessionFactory(new GraphKryoTypes());
    }

    public Folder userGraphFolder()
    {
        var graphFolder = systemPropertyOrEnvironmentVariable("MESAKIT_USER_GRAPH_FOLDER");
        return graphFolder == null ? Folder.desktopFolder() : Folder.parseFolder(this, graphFolder);
    }
}
