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

package com.telenav.mesakit.map.road.name.parser.tokenizer.symbols;

import com.telenav.kivakit.test.UnitTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SymbolTest extends UnitTest
{
    @Test
    public void testEquals()
    {
        var a = Symbol.of("a");
        var a2 = Symbol.of("a");
        var b = Symbol.of("b");
        ensureEqual(a, a2);
        ensureNotEqual(a, b);
        ensureEqual(a.hashCode(), a2.hashCode());
    }
}
