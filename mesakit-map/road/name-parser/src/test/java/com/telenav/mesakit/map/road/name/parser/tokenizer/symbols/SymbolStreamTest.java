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

import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SymbolStreamTest extends UnitTest
{
    @Test
    public void test()
    {
        var in = new SymbolStream(SymbolList.of(" North   125th  Street "));
        in.skipAnyWhiteSpace();
        ensure(in.matches("North"));
        ensure(in.matches(Symbol.WHITESPACE));
        ensure(in.matches("1"));
        ensure(in.matches("2"));
        ensure(in.matches("5"));
        ensure(in.matches("th"));
        ensure(in.matches(Symbol.WHITESPACE));
        ensure(in.matches("Street"));
        in.skipAnyWhiteSpace();
        ensure(!in.hasMore());
    }
}
