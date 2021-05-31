////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.traffic.roadsection.codings.tmc;

import static com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode.FromLongConverter.DIRECTION_MASK;
import static com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode.FromLongConverter.LOCATION_MASK;
import static com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode.FromLongConverter.REGION_MASK;
import static com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode.FromLongConverter.TmcDirection;

public class TmcCodeParser
{
    // Parse code against this regular expression a LOT more efficiently:
    // ([A-Z0-9])(\\d{2})([+-PN])(\\d{5})
    public Long parse(final String code)
    {
        if (code.length() == 9)
        {
            final var countryChar = code.charAt(0);
            if (isUpperCaseLetter(countryChar) || isDigit(countryChar))
            {
                final var countryInt = countryIntFromChar(countryChar);
                if (isDigit(code.charAt(1)) && isDigit(code.charAt(2)))
                {
                    final var regionString = code.substring(1, 3);
                    final var region = Integer.parseInt(regionString);
                    final var directionChar = code.charAt(3);
                    if (isDirection(directionChar))
                    {
                        final var direction = TmcDirection.fromChar(directionChar).integerRepresentation;
                        final var locationString = code.substring(4);
                        if (isDigit(locationString.charAt(0))
                                && isDigit(locationString.charAt(1))
                                && isDigit(locationString.charAt(2))
                                && isDigit(locationString.charAt(3))
                                && isDigit(locationString.charAt(4)))
                        {
                            final var location = Integer.parseInt(locationString);
                            return ((((long) countryInt * REGION_MASK) + region)
                                    * DIRECTION_MASK + direction)
                                    * LOCATION_MASK + location;
                        }
                    }
                }
            }
        }
        return null;
    }

    private int countryIntFromChar(final char countryChar)
    {
        if (countryChar >= '0' && countryChar <= '9')
        {
            return countryChar - '0';
        }
        else if (countryChar >= 'A' && countryChar <= 'F')
        {
            return 10 + countryChar - 'A';
        }
        throw new IllegalArgumentException("Unknown country code: " + countryChar);
    }

    private boolean isDigit(final char character)
    {
        return character >= '0' && character <= '9';
    }

    private boolean isDirection(final char character)
    {
        switch (character)
        {
            case '+':
            case '-':
            case 'P':
            case 'N':
                return true;

            default:
                return false;
        }
    }

    private boolean isUpperCaseLetter(final char character)
    {
        return character >= 'A' && character <= 'Z';
    }
}
