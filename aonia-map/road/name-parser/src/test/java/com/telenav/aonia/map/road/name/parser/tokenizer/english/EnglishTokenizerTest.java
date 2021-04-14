////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.tokenizer.english;

import com.telenav.aonia.map.road.name.parser.locales.english.EnglishTokenizer;
import com.telenav.aonia.map.road.name.parser.locales.english.EnglishUnitedStatesTokenizer;
import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EnglishTokenizerTest extends UnitTest
{
    @Test
    public void test()
    {
        final EnglishTokenizer english = new EnglishUnitedStatesTokenizer();

        final var tokens = english.tokenize("123 Main Street");

        // 1
        ensure(tokens.lookingAt(english.DIGIT));
        ensureEqual("1", tokens.current().text());
        ensure(tokens.match(english.DIGIT));

        // 2
        ensure(tokens.lookingAt(english.DIGIT));
        ensureEqual("2", tokens.current().text());
        ensure(tokens.match(english.DIGIT));

        // 3
        ensure(tokens.lookingAt(english.DIGIT));
        ensureEqual("3", tokens.current().text());
        ensure(tokens.match(english.DIGIT));

        // WHITESPACE
        ensure(tokens.lookingAt(english.WHITESPACE));
        ensureEqual(" ", tokens.current().text());
        ensure(tokens.match(english.WHITESPACE));

        // Main
        ensure(tokens.lookingAt(english.WORD));
        ensureEqual("Main", tokens.current().text());
        ensure(tokens.match(english.WORD));

        // WHITESPACE
        ensure(tokens.lookingAt(english.WHITESPACE));
        ensureEqual(" ", tokens.current().text());
        ensure(tokens.match(english.WHITESPACE));

        // Main
        ensure(tokens.lookingAt(english.STREET));
        ensureEqual("Street", tokens.current().text());
        ensure(tokens.match(english.STREET));

        // END
        ensure(!tokens.hasMore());
    }

    @Test
    public void testInterstate()
    {
        final EnglishTokenizer english = new EnglishUnitedStatesTokenizer();

        {
            final var tokens = english.tokenize("I-5");
            ensure(tokens.match(english.INTERSTATE));
            ensure(tokens.match(english.DIGIT));
        }

        {
            final var tokens = english.tokenize("Interstate 5");
            ensure(tokens.match(english.INTERSTATE));
            ensure(tokens.match(english.DIGIT));
        }
    }

    @Test
    public void testNorthWestComplex()
    {
        final EnglishTokenizer english = new EnglishUnitedStatesTokenizer();
        final var tokens = english.tokenize("North West Main");

        // "North West" is a single token (NORTHWEST), so there should only be three tokens
        ensureEqual(tokens.size(), 3);
        ensureEqual(tokens.current(), english.NORTHWEST);
        tokens.next();
        ensureEqual(tokens.current(), english.WHITESPACE);
        tokens.next();
        ensureEqual(tokens.current(), english.WORD);
        ensureEqual("Main", tokens.current().text());
    }

    @Test
    public void testNorthWestSimple()
    {
        final EnglishTokenizer english = new EnglishUnitedStatesTokenizer();
        final var tokens = english.tokenize("NW Main");

        ensureEqual(tokens.size(), 3);
    }

    @Test
    public void testUsHighway()
    {
        final var english = new EnglishUnitedStatesTokenizer();

        {
            final var tokens = english.tokenize("U.S. 101");
            ensure(tokens.match(english.US_HIGHWAY));
            ensure(tokens.match(english.DIGIT));
        }
    }
}
