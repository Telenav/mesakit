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

package com.telenav.mesakit.map.road.name.parser.tokenizer.english;

import com.telenav.kivakit.testing.UnitTest;
import com.telenav.mesakit.map.road.name.parser.locales.english.EnglishTokenizer;
import com.telenav.mesakit.map.road.name.parser.locales.english.EnglishUnitedStatesTokenizer;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EnglishTokenizerTest extends UnitTest
{
    @Test
    public void test()
    {
        EnglishTokenizer english = new EnglishUnitedStatesTokenizer();

        var tokens = english.tokenize("123 Main Street");

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
        EnglishTokenizer english = new EnglishUnitedStatesTokenizer();

        {
            var tokens = english.tokenize("I-5");
            ensure(tokens.match(english.INTERSTATE));
            ensure(tokens.match(english.DIGIT));
        }

        {
            var tokens = english.tokenize("Interstate 5");
            ensure(tokens.match(english.INTERSTATE));
            ensure(tokens.match(english.DIGIT));
        }
    }

    @Test
    public void testNorthWestComplex()
    {
        EnglishTokenizer english = new EnglishUnitedStatesTokenizer();
        var tokens = english.tokenize("North West Main");

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
        EnglishTokenizer english = new EnglishUnitedStatesTokenizer();
        var tokens = english.tokenize("NW Main");

        ensureEqual(tokens.size(), 3);
    }

    @Test
    public void testUsHighway()
    {
        var english = new EnglishUnitedStatesTokenizer();

        {
            var tokens = english.tokenize("U.S. 101");
            ensure(tokens.match(english.US_HIGHWAY));
            ensure(tokens.match(english.DIGIT));
        }
    }
}
