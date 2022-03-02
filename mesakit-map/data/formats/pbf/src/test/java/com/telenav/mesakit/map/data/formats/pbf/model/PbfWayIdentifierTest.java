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

package com.telenav.mesakit.map.data.formats.pbf.model;

import com.telenav.kivakit.core.test.UnitTest;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import org.junit.Test;

public class PbfWayIdentifierTest extends UnitTest
{
    @Test
    public void testReverse()
    {
        var identifier = new PbfWayIdentifier(5);
        var reversed = identifier.reversed();
        ensure(reversed.isReverse());
        ensureFalse(reversed.isForward());
        ensureEqual(identifier, reversed.reversed());
    }

    @Test
    public void testTypingNegative()
    {
        var identifier = (PbfWayIdentifier) new PbfWayIdentifier(59172301723L).reversed();
        var withType = (PbfWayIdentifier) identifier.withType();
        var withoutType = (PbfWayIdentifier) withType.withoutType();
        ensureEqual(withoutType, identifier);
    }

    @Test
    public void testTypingPositive()
    {
        var identifier = new PbfWayIdentifier(59172301723L);
        var withType = (PbfWayIdentifier) identifier.withType();
        var withoutType = (PbfWayIdentifier) withType.withoutType();
        ensureEqual(withoutType, identifier);
    }
}
