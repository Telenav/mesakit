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
import com.telenav.kivakit.core.io.IO;
import com.telenav.kivakit.resource.serialization.SerializableObject;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionProject;
import com.telenav.mesakit.map.region.RegionUnitTest;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.core.messaging.Listener.emptyListener;
import static com.telenav.kivakit.core.project.Project.resolveProject;
import static com.telenav.kivakit.serialization.core.SerializationSession.SessionType.RESOURCE;

public class RegionIdentityCacheTest extends RegionUnitTest
{
    @Test
    public void test()
    {
        // Create cache
        RegionIdentityCache<State> cache = listenTo(new RegionIdentityCache<>(State.class));

        // Create kryo output to memory
        var session = sessionFactory().newSession(emptyListener());
        var data = new ByteArrayOutputStream(20_000);
        var output = new Output(data);

        // Save the identities
        var version = resolveProject(RegionProject.class).borderDataVersion();
        session.open(output, RESOURCE, kivakit().projectVersion());
        session.write(new SerializableObject<>(identities(), version));
        session.close();
        output.close();

        // Load and create region objects for the identities
        var input = new ByteArrayInputStream(data.toByteArray());
        ensure(cache.load(input, session));
        IO.close(input);

        // Ensure the region objects
        State ca = State.forRegionCode(code("US-CA"));
        ensureEqual("US-CA", ca.identity().iso().code());
        State wa = State.forRegionCode(code("US-WA"));
        ensureEqual("US-WA", wa.identity().iso().code());
        State nm = State.forRegionCode(code("US-NM"));
        ensureEqual("US-NM", nm.identity().iso().code());
    }

    private Set<RegionIdentity> identities()
    {
        Set<RegionIdentity> identities = new HashSet<>();
        identities.add(identity("California", "US-CA", "United_States-California"));
        identities.add(identity("Washington", "US-WA", "United_States-Washington"));
        identities.add(identity("New Mexico", "US-NM", "United_States-New_Mexico"));
        return identities;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private RegionIdentity identity(String name, String iso, String mesakit)
    {
        return new RegionIdentity(name)
                .withIdentifier(new RegionIdentifier(0))
                .withIsoCode(code(iso))
                .withMesaKitCode(code(mesakit));
    }
}
