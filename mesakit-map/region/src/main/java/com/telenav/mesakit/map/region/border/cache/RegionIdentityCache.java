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

package com.telenav.mesakit.map.region.border.cache;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.KivaKit;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.language.values.version.VersionedObject;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.serialization.core.SerializationSession;
import com.telenav.mesakit.core.MesaKit;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionProject;

import java.io.InputStream;
import java.util.Set;

import static com.telenav.kivakit.serialization.core.SerializationSession.Type.RESOURCE;

/**
 * Holds a set of region identities for a given region type, so they can be quickly loaded, creating a region object for
 * each identity, without loading the borders (either from PBF or Kryo) to find the identities.
 *
 * @author jonathanl (shibo)
 */
public class RegionIdentityCache<T extends Region<T>> extends BaseRepeater
{
    private final Class<T> type;

    public RegionIdentityCache(Class<T> type)
    {
        this.type = type;
    }

    /**
     * Load the region codes from, the serialized file, file that was created from reading borders.
     */
    public synchronized boolean load(File cacheFile, SerializationSession session)
    {
        // We're loading identities
        var region = type().getSimpleName();
        trace("Loading $ identities from $", region, cacheFile);

        // so open cache file for reading
        try (var input = cacheFile.openForReading())
        {
            // and load the identities
            return load(input, session);
        }
        catch (Exception e)
        {
            trace(e, "Unable to load $ identifies from $", region, cacheFile);
        }
        return false;
    }

    public synchronized boolean load(InputStream input, SerializationSession session)
    {
        var start = Time.now();

        // Read the KivaKit version that wrote the data
        var kivakitVersion = session.open(RESOURCE, KivaKit.get().projectVersion(), input);

        // read the set of identities
        VersionedObject<Set<RegionIdentity>> identities = session.read();
        if (identities != null)
        {
            trace("Region identities cache file is version $, written by KivaKit version $", identities.version(), kivakitVersion);

            // ensure that this MesaKit version can read the data (data is backwards compatible but not forward),
            if (MesaKit.get().projectVersion().isNewerThanOrEqualTo(RegionProject.get().borderDataVersion()))
            {
                // and loop through them
                for (var identity : identities.get())
                {
                    // creating the region object for each identity if it doesn't already exist.
                    identity.findOrCreateRegion(type());
                }
                trace("Loaded $ identities from cache in $", Count.count(identities.get()), start.elapsedSince());
            }
            return true;
        }

        return false;
    }

    public void save(SerializationSession session,
                     Version version,
                     Set<RegionIdentity> identities)
    {
        session.write(new VersionedObject<>(version, identities));
        session.close();
    }

    private Class<T> type()
    {
        return type;
    }
}
