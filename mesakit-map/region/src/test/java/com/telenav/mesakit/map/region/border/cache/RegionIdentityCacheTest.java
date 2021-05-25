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

import com.esotericsoftware.kryo.io.Output;
import com.telenav.kivakit.kernel.KivaKit;
import com.telenav.kivakit.kernel.language.io.IO;
import com.telenav.kivakit.kernel.language.values.version.VersionedObject;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.project.MapRegionProject;
import com.telenav.mesakit.map.region.project.MapRegionUnitTest;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.serialization.core.SerializationSession.Type.RESOURCE;

public class RegionIdentityCacheTest extends MapRegionUnitTest
{
    @Test
    public void test()
    {
        // Create cache
        final RegionIdentityCache<State> cache = listenTo(new RegionIdentityCache<>(State.class));

        // Create kryo output to memory
        final var session = sessionFactory().session(Listener.none());
        final var data = new ByteArrayOutputStream(20_000);
        final var output = new Output(data);

        // Save the identities
        final var version = MapRegionProject.get().borderDataVersion();
        session.open(RESOURCE, KivaKit.get().version(), output);
        session.write(new VersionedObject<>(version, identities()));
        session.close();
        output.close();

        // Load and create region objects for the identities
        final var input = new ByteArrayInputStream(data.toByteArray());
        ensure(cache.load(input, session));
        IO.close(input);

        // Ensure the region objects
        final State ca = State.forRegionCode(code("US-CA"));
        ensureEqual("US-CA", ca.identity().iso().code());
        final State wa = State.forRegionCode(code("US-WA"));
        ensureEqual("US-WA", wa.identity().iso().code());
        final State nm = State.forRegionCode(code("US-NM"));
        ensureEqual("US-NM", nm.identity().iso().code());
    }

    private Set<RegionIdentity> identities()
    {
        final Set<RegionIdentity> identities = new HashSet<>();
        identities.add(identity("California", "US-CA", "United_States-California"));
        identities.add(identity("Washington", "US-WA", "United_States-Washington"));
        identities.add(identity("New Mexico", "US-NM", "United_States-New_Mexico"));
        return identities;
    }

    private RegionIdentity identity(final String name, final String iso, final String mesakit)
    {
        return new RegionIdentity(name)
                .withIdentifier(new RegionIdentifier(0))
                .withIsoCode(code(iso))
                .withMesaKitCode(code(mesakit));
    }
}
