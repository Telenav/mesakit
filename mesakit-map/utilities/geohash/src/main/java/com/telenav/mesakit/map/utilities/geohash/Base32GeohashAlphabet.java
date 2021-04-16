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

package com.telenav.mesakit.map.utilities.geohash;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Errors by code length:
 * <table border='1'>
 * <caption>Error codes by length</caption>
 * <tr>
 * <th>geohash code length</th>
 * <th>error</th>
 * </tr>
 * <tr>
 * <td>6 characters</td>
 * <td>less than 1 kilometer</td>
 * </tr>
 * <tr>
 * <td>10 characters</td>
 * <td>less than 1 meter</td>
 * </tr>
 * <tr>
 * <td>15 characters</td>
 * <td>less than 1 millimeter</td>
 * </tr>
 * </table>
 *
 * @author Mihai Chintoanu
 */
class Base32GeohashAlphabet implements GeohashAlphabet
{
    public static final Base32GeohashAlphabet INSTANCE = new Base32GeohashAlphabet();

    private final char[] characters = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final HashMap<Character, Integer> indexForCharacter = new HashMap<>();

    // the bits per character is log[2] of the total number of characters
    // because there is no log[2] in Math, we use log(x) / log(2), which is equivalent to log[2](x)
    private final int bitsPerCharacter = (int) Math.ceil(Math.log(characters.length) / Math.log(2));

    private Base32GeohashAlphabet()
    {
        var i = 0;
        for (final var c : characters)
        {
            indexForCharacter.put(c, i++);
        }
    }

    @Override
    public int bitsPerCharacter()
    {
        return bitsPerCharacter;
    }

    @Override
    public Collection<Character> characters()
    {
        return indexForCharacter.keySet();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Base32GeohashAlphabet)
        {
            final var that = (Base32GeohashAlphabet) object;
            return Arrays.equals(characters, that.characters);
        }
        return false;
    }

    @Override
    public char get(final int index)
    {
        return characters[index];
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(characters);
    }

    @Override
    public int indexOf(final char character)
    {
        return indexForCharacter.get(character);
    }

    @Override
    public boolean isValid(final String text)
    {
        var valid = text != null && text.length() <= maximumTextLength();
        for (var i = 0; valid && i < text.length(); i++)
        {
            if (!indexForCharacter.containsKey(text.charAt(i)))
            {
                valid = false;
                break;
            }
        }
        return valid;
    }

    @Override
    public int maximumTextLength()
    {
        return 15;
    }

    @Override
    public int numberOfCharacters()
    {
        return characters.length;
    }
}
