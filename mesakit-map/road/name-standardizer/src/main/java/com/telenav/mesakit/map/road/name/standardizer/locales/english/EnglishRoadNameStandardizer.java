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

package com.telenav.mesakit.map.road.name.standardizer.locales.english;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.parser.ParsedRoadName;
import com.telenav.mesakit.map.road.name.parser.RoadNameParser;
import com.telenav.mesakit.map.road.name.standardizer.BaseRoadNameStandardizer;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.core.ensure.Ensure.fail;
import static com.telenav.mesakit.map.road.name.parser.ParsedRoadName.DirectionFormat.PREFIXED;

@SuppressWarnings("SpellCheckingInspection")
public class EnglishRoadNameStandardizer extends BaseRoadNameStandardizer
{
    private static final Debug DEBUG = new Debug(LoggerFactory.newLogger());

    private final ThreadLocal<RoadNameParser> parser;

    private final Map<String, String> baseName = new HashMap<>();

    private final Map<String, String> type = new HashMap<>();

    private Mode mode;

    public EnglishRoadNameStandardizer(MapLocale locale)
    {
        parser = ThreadLocal.withInitial(() -> RoadNameParser.get(locale));
    }

    @Override
    public void mode(Mode mode)
    {
        this.mode = mode;
        initialize(mode);
    }

    @Override
    public ParsedRoadName standardize(RoadName name)
    {
        if (mode == null)
        {
            return fail("Must set standardizer mode");
        }
        try
        {
            var parsed = parser.get().parse(name);
            if (parsed != null)
            {
                return standardize(parsed);
            }
        }
        catch (Exception e)
        {
            DEBUG.warning(e, "Unable to standardize '$'", name);
        }
        return null;
    }

    private void initialize(Mode mode)
    {
        baseName.put("Metro", "Metropolitan");
        baseName.put("Saint", "St");

        type.put("Arcade", "Arc");
        type.put("Avenue", "Ave");
        type.put("Beach", "Bch");
        type.put("Boulevard", "Blvd");
        type.put("Branch", "Br");
        type.put("Bridge", "Brg");
        type.put("Brook", "Brk");
        type.put("BusLoop", "Bl");
        type.put("Bypass", "Byp");
        type.put("Camp", "Cp");
        type.put("Canyon", "Cyn");
        type.put("Center", "Ctr");
        type.put("Circle", "Cir");
        type.put("Club", "Clb");
        type.put("Connector", "Conn");
        type.put("Corners", "Cors");
        type.put("Court", "Ct");
        type.put("Cove", "Cv");
        type.put("Crest", "Crst");
        type.put("Crescent", "Cres");
        type.put("Crossing", "Xing");
        type.put("Dale", "Dl");
        type.put("Dam", "Dm");
        type.put("Drive", "Dr");
        type.put("Driveway", "Drwy");
        type.put("Estate", "Est");
        type.put("Estates", "Ests");
        type.put("Expressway", "Expy");
        type.put("Extension", "Ext");
        type.put("Field", "Fld");
        type.put("Flats", "Flts");
        type.put("Freeway", "Fwy");
        type.put("Forest", "Frst");
        type.put("Garden", "Gdn");
        type.put("Gardens", "Gdns");
        type.put("Gateway", "Gtwy");
        type.put("Glen", "Gln");
        type.put("Green", "Grn");
        type.put("Grove", "Grv");
        type.put("Heights", "Hts");
        type.put("Highway", "Hwy");
        type.put("Hill", "Hl");
        type.put("Hills", "Hls");
        type.put("Junction", "Jct");
        type.put("Lane", "Ln");
        type.put("Landing", "Lndg");
        type.put("Lanes", "Ln");
        type.put("Monitor", "Lck");
        type.put("Meadow", "Mdw");
        type.put("Meadows", "Mdws");
        type.put("Mills", "Mls");
        type.put("Motorway", "Mtwy");
        type.put("Mountain", "Mtn");
        type.put("Overpass", "Opas");
        type.put("Parkway", "Pkwy");
        type.put("Passage", "Psge");
        if (mode == Mode.MESAKIT_STANDARDIZATION)
        {
            type.put("Park", "Pk");
            type.put("Place", "Pl");
            type.put("Point", "Pt");
        }
        type.put("Pines", "Pnes");
        type.put("Ridge", "Rdg");
        type.put("Road", "Rd");
        type.put("Roads", "Rds");
        type.put("Route", "Rte");
        type.put("Shores", "Shrs");
        type.put("Skyway", "Skwy");
        type.put("Springs", "Spgs");
        type.put("Spur", "Spur");
        type.put("Square", "Sq");
        type.put("StateRoute", "SR");
        type.put("Station", "Sta");
        type.put("Street", "St");
        type.put("Track", "Trk");
        type.put("Terrace", "Ter");
        type.put("Throughway", "Trwy");
        type.put("Trafficway", "Trfwy");
        type.put("Trail", "Trl");
        type.put("Tunnel", "Tunl");
        type.put("Turnpike", "Tpke");
        type.put("Underpass", "Upas");
        type.put("Viaduct", "Via");
    }

    private ParsedRoadName standardize(ParsedRoadName name)
    {
        var builder = new ParsedRoadName.Builder(name);
        builder.type(standardizedType(name.type()), name.rawType());
        builder.baseName(standardizedBaseName(name), name.rawBaseName());
        return builder.build();
    }

    private String standardizedBaseName(ParsedRoadName name)
    {
        var words = StringList.words(name.baseName());
        for (var i = 0; i < words.size(); i++)
        {
            var standardized = baseName.get(words.get(i));
            if (standardized != null)
            {
                words.set(i, standardized);
            }
            // If we're looking at the first word of a multi-word base name and there isn't a
            // direction prefix (as in East North Water St), then we can change that first word to
            // an abbreviation
            if (i == 0 && words.size() > 1 && name.directionFormat() != PREFIXED)
            {
                switch (words.get(i))
                {
                    case "North":
                        words.set(i, "N");
                        break;
                    case "South":
                        words.set(i, "S");
                        break;
                    case "East":
                        words.set(i, "E");
                        break;
                    case "West":
                        words.set(i, "W");
                        break;
                }
            }
        }
        return words.join(' ');
    }

    private String standardizedType(String type)
    {
        var standardized = this.type.get(type);
        return standardized != null ? standardized : type;
    }
}
