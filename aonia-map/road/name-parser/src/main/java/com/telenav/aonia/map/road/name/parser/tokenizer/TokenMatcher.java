////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.tokenizer;

import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.SymbolStream;

@FunctionalInterface
public interface TokenMatcher
{
    /**
     * @param symbols Stream of symbols to match against
     * @return The token matching the symbols
     */
    Token match(final SymbolStream symbols);
}
