////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.tokenizer.symbols;

import com.telenav.kivakit.core.test.UnitTest;
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
