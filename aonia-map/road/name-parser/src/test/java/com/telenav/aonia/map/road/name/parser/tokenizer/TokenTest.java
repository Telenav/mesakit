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

package com.telenav.aonia.map.road.name.parser.tokenizer;

import com.telenav.aonia.map.road.name.parser.locales.english.EnglishTokenizer;
import com.telenav.aonia.map.road.name.parser.locales.english.EnglishUnitedStatesTokenizer;
import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.SymbolList;
import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TokenTest extends UnitTest
{
    @Test
    public void test()
    {
        final EnglishTokenizer english = new EnglishUnitedStatesTokenizer();

        final var list = SymbolList.of("123");

        final var first = english.next(list.asStream());
        ensureEqual(english.DIGIT, first);
        ensureEqual("1", first.text());

        final var second = english.next(list.subList(1).asStream());
        ensureEqual(english.DIGIT, second);
        ensureEqual("2", second.text());

        final var third = english.next(list.subList(2).asStream());
        ensureEqual(english.DIGIT, third);
        ensureEqual("3", third.text());
    }

    @Test
    public void testRoadTypes()
    {
        final EnglishTokenizer english = new EnglishUnitedStatesTokenizer();

        final var list = SymbolList.of("ave");

        final var first = english.next(list.asStream());
        ensureEqual(english.AVENUE, first);
    }
}
