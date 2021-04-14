////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
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
