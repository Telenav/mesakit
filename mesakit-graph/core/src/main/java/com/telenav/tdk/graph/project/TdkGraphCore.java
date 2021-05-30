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

package com.telenav.tdk.graph.project;

import com.telenav.tdk.core.filesystem.Folder;
import com.telenav.tdk.core.kernel.language.collections.set.Sets;
import com.telenav.tdk.core.kernel.language.io.serialization.TdkSerializer;
import com.telenav.tdk.core.kernel.language.object.Lazy;
import com.telenav.tdk.core.kernel.language.vm.JavaVirtualMachine;
import com.telenav.tdk.core.kernel.project.TdkProject;
import com.telenav.tdk.data.formats.pbf.processing.filters.PbfFilters;
import com.telenav.tdk.data.formats.pbf.project.TdkDataFormatsPbf;
import com.telenav.tdk.graph.traffic.project.TdkGraphTraffic;
import com.telenav.tdk.map.region.project.TdkMapRegion;

import java.util.Set;

/**
 * @author jonathanl (shibo)
 */
public class TdkGraphCore extends TdkProject
{
    private static final Lazy<TdkGraphCore> singleton = new Lazy<>(TdkGraphCore::new);

    public static TdkGraphCore get()
    {
        return singleton.get();
    }

    public static void main(final String[] args)
    {
        get().showDependencies();
    }

    protected TdkGraphCore()
    {
        System.setProperty("tdk.graph.folder", graphFolder().toString());
        JavaVirtualMachine.local().invalidateProperties();
    }

    @Override
    public Set<TdkProject> dependencies()
    {
        return Sets.of(TdkMapRegion.get(), TdkGraphTraffic.get(), TdkDataFormatsPbf.get());
    }

    /**
     * @return The graph folder where various kinds of data are cached
     */
    public Folder graphFolder()
    {
        return Folder.tdkCacheFolder()
                .folder("graph")
                .mkdirs();
    }

    @Override
    public TdkSerializer newSerializer()
    {
        return new TdkGraphCoreKryoSerializer();
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

    public Folder userGraphFolder()
    {
        final var graphFolder = JavaVirtualMachine.property("TDK_USER_GRAPH_FOLDER");
        return graphFolder == null ? Folder.desktop() : new Folder(graphFolder);
    }
}
