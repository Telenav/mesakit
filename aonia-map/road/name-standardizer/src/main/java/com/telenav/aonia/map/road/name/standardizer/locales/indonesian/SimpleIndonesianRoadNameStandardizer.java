////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.standardizer.locales.indonesian;

import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.locales.indonesian.IndonesianTokenizer;
import com.telenav.aonia.map.road.name.parser.tokenizer.Token;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;

import java.util.HashMap;
import java.util.Map;

/**
 * Takes {@link RoadName} and standardizes it.
 *
 * @author jonathanl (shibo)
 */
public class SimpleIndonesianRoadNameStandardizer
{
    private static final Debug DEBUG = new Debug(LoggerFactory.newLogger());

    final IndonesianTokenizer TOKENIZER = new IndonesianTokenizer();

    private final Map<Token, String> direction = new HashMap<>();

    private final Map<Token, String> roadType = new HashMap<>();

    private final Map<Token, String> properName = new HashMap<>();

    public RoadName standardize(final RoadName name)
    {
        try
        {
            final var builder = new StringBuilder();
            for (final var token : TOKENIZER.tokenize(name.name()))
            {
                builder.append(standardize(token));
            }
            return RoadName.forName(builder.toString());
        }
        catch (final Exception e)
        {
            DEBUG.warning(e, "Unable to standardize '$'", name);
        }
        return null;
    }

    private String standardize(final Token token)
    {
        String standardized = null;
        if (TOKENIZER.isCardinalDirection(token) || TOKENIZER.isOctant(token)
                || TOKENIZER.isQuadrant(token))
        {
            standardized = direction.get(token);
        }
        if (TOKENIZER.isRoadType(token))
        {
            standardized = roadType.get(token);
        }
        if (TOKENIZER.isProperName(token))
        {
            standardized = properName.get(token);
        }
        return standardized != null ? standardized : token.text();
    }

    {
        direction.put(TOKENIZER.NORTH, "U");
        direction.put(TOKENIZER.SOUTH, "S");
        direction.put(TOKENIZER.EAST, "T");
        direction.put(TOKENIZER.WEST, "B");
        direction.put(TOKENIZER.NORTHEAST, "TL");
        direction.put(TOKENIZER.NORTHWEST, "BL");
        direction.put(TOKENIZER.SOUTHEAST, "TG");
        direction.put(TOKENIZER.SOUTHWEST, "BD");
        direction.put(TOKENIZER.NORTH_NORTHEAST, "UTL");
        direction.put(TOKENIZER.NORTH_NORTHWEST, "UBL");
        direction.put(TOKENIZER.SOUTH_SOUTHEAST, "SM");
        direction.put(TOKENIZER.SOUTH_SOUTHWEST, "SBD");
        direction.put(TOKENIZER.EAST_NORTHEAST, "TTL");
        direction.put(TOKENIZER.EAST_SOUTHEAST, "TM");
        direction.put(TOKENIZER.WEST_SOUTHWEST, "BBD");
        direction.put(TOKENIZER.WEST_NORTHWEST, "BBL");
    }

    {
        roadType.put(TOKENIZER.JALAN_STREET, "Jl");
        roadType.put(TOKENIZER.GANG_ALLEY, "Gg");
    }

    {
        properName.put(TOKENIZER.ABDUL_NAME, "Abd");
        properName.put(TOKENIZER.AWANG_HONORIFIC, "Awg");
        properName.put(TOKENIZER.DAYANG_HONORIFIC, "Dyg");
        properName.put(TOKENIZER.DEPARTEMEN_DEPARTMENT, "Dep");
        properName.put(TOKENIZER.DOKTER_DOCTOR, "Dr");
        properName.put(TOKENIZER.GUNUNG_MOUNTAIN, "Gn");
        properName.put(TOKENIZER.HAJI_PILGRIM, "Hj");
        properName.put(TOKENIZER.INSINYUR_ENGINEER, "Ir");
        properName.put(TOKENIZER.JENDERAL_GENERAL, "Jend");
        properName.put(TOKENIZER.KAMPUNG_TOWN, "Kp");
        properName.put(TOKENIZER.KAROMAH_NAME, "KH");
        properName.put(TOKENIZER.KAVLING_PLOT, "Kav");
        properName.put(TOKENIZER.KOLONEL_COLONEL, "Kol");
        properName.put(TOKENIZER.KOMPLEK_COMPLEX, "Komp");
        properName.put(TOKENIZER.MOHAMMAD_NAME, "Moh");
        properName.put(TOKENIZER.MUHAMMAD_NAME, "Muh");
        properName.put(TOKENIZER.PASAR_MARKET, "Ps");
        properName.put(TOKENIZER.PENGIRAN_HONORIFIC, "Pg");
        properName.put(TOKENIZER.PERGUDANGAN_WAREHOUSING, "Perg");
        properName.put(TOKENIZER.PROFESOR_PROFESSOR, "Prof");
        properName.put(TOKENIZER.SIMPANG_INTERSECTION, "Spg");
        properName.put(TOKENIZER.SUNGAI_RIVER, "Sg");
        properName.put(TOKENIZER.TANJUNG_CAPE, "Tj");
    }
}
