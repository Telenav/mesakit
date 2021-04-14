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
public class SymbolStreamTest extends UnitTest
{
    @Test
    public void test()
    {
        final var in = new SymbolStream(SymbolList.of(" North   125th  Street "));
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
