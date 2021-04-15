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

package com.telenav.aonia.map.road.name.parser.locales.indonesian;

import com.telenav.aonia.map.road.name.parser.tokenizer.Token;
import com.telenav.aonia.map.road.name.parser.tokenizer.Tokenizer;

import java.util.HashSet;
import java.util.Set;

/**
 * Tokenizer for Indonesian road names. Breaks down an input string into {@link Token} objects that are more easily
 * handled in parsing.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SpellCheckingInspection")
public class IndonesianTokenizer extends Tokenizer
{
    // N, S, E, W
    private final Set<Token> cardinalDirections = new HashSet<>();

    // Quadrants are like NE, NW, SE, SW
    private final Set<Token> quadrants = new HashSet<>();

    // Octants are like NNE, NNW, SSE, SSW, etc.
    private final Set<Token> octants = new HashSet<>();

    // Road types are like "Boulevard", "Avenue", "Street", etc.
    private final Set<Token> roadTypes = new HashSet<>();

    // Names of people, places and things. In Indonesia, this includes honorifics.
    private final Set<Token> properNames = new HashSet<>();

    // Directions

    // NOTE: The octants and quadrants are above the definitions for the cardinal directions so they
    // will get priority in evaluation (otherwise, the input "NORTH WEST" would always result in the
    // token NORTH followed by WEST and never NORTHWEST)

    ///////////////////////////// OCTANTS /////////////////////////////

    public final Token NORTH_NORTHEAST = create("North-Northeast")
            .matchesAnyOf("UTL")
            .matchesSequence("UTARA", " ", "TIMUR", " ", "LAUT")
            .addTo(octants);

    public final Token NORTH_NORTHWEST = create("North-Northwest")
            .matchesAnyOf("UBL")
            .matchesSequence("UTARA", " ", "BARAT", " ", "LAUT")
            .addTo(octants);

    public final Token SOUTH_SOUTHEAST = create("South-Southeast")
            .matchesAnyOf("SM")
            .matchesSequence("SELATAN", " ", "MENENGGARA")
            .addTo(octants);

    public final Token SOUTH_SOUTHWEST = create("South-Southwest")
            .matchesAnyOf("SBD")
            .matchesSequence("SELATAN", " ", "BARAT", "DAYA")
            .addTo(octants);

    public final Token EAST_NORTHEAST = create("East-Northeast")
            .matchesAnyOf("TTL")
            .matchesSequence("TIMUR", " ", "TIMUR", " ", "LAUT")
            .addTo(octants);

    public final Token EAST_SOUTHEAST = create("East-Southeast")
            .matchesAnyOf("TM")
            .matchesSequence("TIMUR", " ", "MENENGGARA")
            .addTo(octants);

    public final Token WEST_SOUTHWEST = create("West-Southwest")
            .matchesAnyOf("BBD")
            .matchesSequence("BARAT", " ", "BARAT", "DAYA")
            .addTo(octants);

    public final Token WEST_NORTHWEST = create("West-Northwest")
            .matchesAnyOf("BBL")
            .matchesSequence("BARAT", " ", "BARAT", " ", "LAUT")
            .addTo(octants);

    ///////////////////////////// QUADRANTS /////////////////////////////

    public final Token NORTHEAST = create("Northeast")
            .matchesAnyOf("TL")
            .matchesSequence("TIMUR", " ", "LAUT")
            .addTo(quadrants);

    public final Token NORTHWEST = create("Northwest")
            .matchesAnyOf("BL")
            .matchesSequence("BARAT", " ", "LAUT")
            .addTo(quadrants);

    public final Token SOUTHEAST = create("Southeast")
            .matchesAnyOf("TG", "TENGGARA")
            .addTo(quadrants);

    public final Token SOUTHWEST = create("Southwest")
            .matchesAnyOf("BD")
            .matchesSequence("BARAT", "DAYA")
            .addTo(quadrants);

    ///////////////////////////// CARDINAL DIRECTIONS /////////////////////////////

    public final Token NORTH = create("North")
            .matchesAnyOf("UTARA", "U")
            .addTo(cardinalDirections);

    public final Token SOUTH = create("South")
            .matchesAnyOf("SELATAN", "S")
            .addTo(cardinalDirections);

    public final Token EAST = create("East")
            .matchesAnyOf("TIMUR", "T")
            .addTo(cardinalDirections);

    public final Token WEST = create("West")
            .matchesAnyOf("BARAT", "B")
            .addTo(cardinalDirections);

    ///////////////////////////// ROAD TYPES /////////////////////////////

    public final Token GANG_ALLEY = create("Gang")
            .matchesAnyOf("Gang", "Gg")
            .addTo(roadTypes);

    public final Token JALAN_STREET = create("Jalan").
            matchesAnyOf("Jalan", "Jl", "Jln").
            addTo(roadTypes);

    ///////////////////////////// NUMBERS /////////////////////////////

    public final Token DIGIT = create("Digit")
            .matchesAnyOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

    ///////////////////////////// PROPER NAMES /////////////////////////////

    public final Token ABDUL_NAME = create("Abdul")
            .matchesAnyOf("Abdul", "Abd")
            .addTo(properNames);

    public final Token AWANG_HONORIFIC = create("Awang")
            .matchesAnyOf("Awang", "Awg")
            .addTo(properNames);

    public final Token DAYANG_HONORIFIC = create("Dayang")
            .matchesAnyOf("Dayang", "Dyg")
            .addTo(properNames);

    public final Token DEPARTEMEN_DEPARTMENT = create("Departemen")
            .matchesAnyOf("Departemen", "Dep")
            .addTo(properNames);

    public final Token DOKTER_DOCTOR = create("Dokter")
            .matchesAnyOf("Dokter", "Dr")
            .addTo(properNames);

    public final Token GUNUNG_MOUNTAIN = create("Gunung")
            .matchesAnyOf("Gunung", "Gn")
            .addTo(properNames);

    public final Token HAJI_PILGRIM = create("Haji")
            .matchesAnyOf("Haji", "Hj", "H")
            .addTo(properNames);

    public final Token INSINYUR_ENGINEER = create("Insinyur")
            .matchesAnyOf("Insinyur", "Ir")
            .addTo(properNames);

    public final Token JENDERAL_GENERAL = create("Jenderal")
            .matchesAnyOf("Jenderal", "Jend")
            .addTo(properNames);

    public final Token KAMPUNG_TOWN = create("Kampong")
            .matchesAnyOf("Kampong", "Kampung", "Kp")
            .addTo(properNames);

    public final Token KAROMAH_NAME = create("Karomah")
            .matchesAnyOf("Karomah", "KH")
            .addTo(properNames);

    public final Token KAVLING_PLOT = create("Kavling")
            .matchesAnyOf("Kavling", "Kav")
            .addTo(properNames);

    public final Token KOLONEL_COLONEL = create("Kolonel")
            .matchesAnyOf("Kolonel", "Kol")
            .addTo(properNames);

    public final Token KOMPLEK_COMPLEX = create("Komplek")
            .matchesAnyOf("Komplek", "Komp")
            .addTo(properNames);

    public final Token MOHAMMAD_NAME = create("Mohammad")
            .matchesAnyOf("Mohammad", "Moh")
            .addTo(properNames);

    public final Token MUHAMMAD_NAME = create("Muhammad")
            .matchesAnyOf("Muhammad", "Muh")
            .addTo(properNames);

    public final Token PENGIRAN_HONORIFIC = create("Pengiran")
            .matchesAnyOf("Pengiran", "Pg")
            .addTo(properNames);

    public final Token PERGUDANGAN_WAREHOUSING = create("Pergudangan")
            .matchesAnyOf("Pergudangan", "Perg")
            .addTo(properNames);

    public final Token PASAR_MARKET = create("Pasar")
            .matchesAnyOf("Pasar", "Ps")
            .addTo(properNames);

    public final Token PROFESOR_PROFESSOR = create("Profesor")
            .matchesAnyOf("Profesor", "Prof")
            .addTo(properNames);

    public final Token SIMPANG_INTERSECTION = create("Simpang")
            .matchesAnyOf("Simpang", "Spg")
            .addTo(properNames);

    public final Token SUNGAI_RIVER = create("Sungai")
            .matchesAnyOf("Sungai", "Sg")
            .addTo(properNames);

    public final Token TANJUNG_CAPE = create("Tanjung")
            .matchesAnyOf("Tanjung", "Tj")
            .addTo(properNames);

    public final Token OPEN_PARENTHESIS = create("OpenParenthesis")
            .matchesAnyOf("(");

    public final Token CLOSE_PARENTHESIS = create("CloseParenthesis")
            .matchesAnyOf(")");

    public boolean isCardinalDirection(final Token token)
    {
        return cardinalDirections.contains(token);
    }

    public boolean isCloseParenthesis(final Token token)
    {
        return token != null && token.equals(CLOSE_PARENTHESIS);
    }

    public boolean isDigit(final Token token)
    {
        return token != null && token.equals(DIGIT);
    }

    public boolean isOctant(final Token token)
    {
        return octants.contains(token);
    }

    public boolean isOpenParenthesis(final Token token)
    {
        return token != null && token.equals(OPEN_PARENTHESIS);
    }

    public boolean isProperName(final Token token)
    {
        return properNames.contains(token);
    }

    public boolean isQuadrant(final Token token)
    {
        return quadrants.contains(token);
    }

    public boolean isRoadType(final Token token)
    {
        return roadTypes.contains(token);
    }

    public boolean isRomanNumeral(final Token token)
    {
        return token.text().matches("[XIV]+");
    }
}
