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

package com.telenav.kivakit.graph.project;

import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.collections.set.Sets;
import com.telenav.kivakit.kernel.language.io.serialization.KivaKitSerializer;
import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.project.KivaKitProject;
import com.telenav.kivakit.data.formats.pbf.processing.filters.PbfFilters;
import com.telenav.kivakit.data.formats.pbf.project.KivaKitDataFormatsPbf;
import com.telenav.kivakit.graph.traffic.project.KivaKitGraphTraffic;
import com.telenav.kivakit.map.region.project.KivaKitMapRegion;

import java.util.Set;

/**
 * @author jonathanl (shibo)
 */
public class KivaKitGraphCore extends KivaKitProject
{
    private static final Lazy<KivaKitGraphCore> singleton = new Lazy<>(KivaKitGraphCore::new);

    public static KivaKitGraphCore get()
    {
        return singleton.get();
    }

    public static void main(final String[] args)
    {
        get().showDependencies();
    }

    protected KivaKitGraphCore()
    {
        System.setProperty("tdk.graph.folder", graphFolder().toString());
        JavaVirtualMachine.local().invalidateProperties();
    }

    @Override
    public Set<KivaKitProject> dependencies()
    {
        return Sets.of(KivaKitMapRegion.get(), KivaKitGraphTraffic.get(), KivaKitDataFormatsPbf.get());
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
    public KivaKitSerializer newSerializer()
    {
        return new KivaKitGraphCoreKryoSerializer();
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
