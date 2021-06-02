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

package com.telenav.mesakit.graph.project;

import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.collections.set.Sets;
import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.project.Project;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.PbfFilters;
import com.telenav.mesakit.map.data.formats.pbf.project.DataFormatsPbfProject;
import com.telenav.mesakit.map.region.project.MapRegionProject;

import java.util.Set;

/**
 * @author jonathanl (shibo)
 */
public class GraphCoreProject extends Project
{
    private static final Lazy<GraphCoreProject> singleton = Lazy.of(GraphCoreProject::new);

    public static GraphCoreProject get()
    {
        return singleton.get();
    }

    protected GraphCoreProject()
    {
        System.setProperty("tdk.graph.folder", graphFolder().toString());
        JavaVirtualMachine.local().invalidateProperties();
    }

    @Override
    public Set<Project> dependencies()
    {
        return Sets.of(MapRegionProject.get(), DataFormatsPbfProject.get());
    }

    /**
     * @return The graph folder where various kinds of data are cached
     */
    public Folder graphFolder()
    {
        return Folder.kivakitCache()
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
        return new GraphCoreKryoTypes().sessionFactory();
    }

    public Folder userGraphFolder()
    {
        final var graphFolder = JavaVirtualMachine.property("TDK_USER_GRAPH_FOLDER");
        return graphFolder == null ? Folder.desktop() : Folder.parse(graphFolder);
    }
}
