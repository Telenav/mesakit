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
public class SymbolListTest extends UnitTest
{
    @SuppressWarnings("UnusedAssignment")
    @Test
    public void test()
    {
        final SymbolList list = SymbolList.of(" North   125th  Street ");
        var i = 0;
        ensureEqual(Symbol.WHITESPACE, list.get(i++));
        ensureEqual(Symbol.of("North"), list.get(i++));
        ensureEqual(Symbol.WHITESPACE, list.get(i++));
        ensureEqual(Symbol.of("1"), list.get(i++));
        ensureEqual(Symbol.of("2"), list.get(i++));
        ensureEqual(Symbol.of("5"), list.get(i++));
        ensureEqual(Symbol.of("th"), list.get(i++));
        ensureEqual(Symbol.WHITESPACE, list.get(i++));
        ensureEqual(Symbol.of("Street"), list.get(i++));
        ensureEqual(Symbol.WHITESPACE, list.get(i++));
    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    public void test2()
    {
        final SymbolList list = SymbolList.of("I-5 North");
        var i = 0;
        ensureEqual(Symbol.of("I"), list.get(i++));
        ensureEqual(Symbol.DASH, list.get(i++));
        ensureEqual(Symbol.of("5"), list.get(i++));
        ensureEqual(Symbol.WHITESPACE, list.get(i++));
        ensureEqual(Symbol.of("North"), list.get(i++));
    }
}
