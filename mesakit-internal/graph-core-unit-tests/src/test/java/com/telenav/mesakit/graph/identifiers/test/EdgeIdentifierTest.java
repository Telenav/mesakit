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

package com.telenav.mesakit.graph.identifiers.test;

import com.telenav.mesakit.graph.core.test.GraphUnitTest;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class EdgeIdentifierTest extends GraphUnitTest
{
    @Test
    public void testEquals()
    {
        final long id = 1234567;
        ensureEqual(new EdgeIdentifier(id), new EdgeIdentifier(id));
    }
}
