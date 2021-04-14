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
public class SymbolTest extends UnitTest
{
    @Test
    public void testEquals()
    {
        final var a = Symbol.of("a");
        final var a2 = Symbol.of("a");
        final var b = Symbol.of("b");
        ensureEqual(a, a2);
        ensureNotEqual(a, b);
        ensureEqual(a.hashCode(), a2.hashCode());
    }
}
