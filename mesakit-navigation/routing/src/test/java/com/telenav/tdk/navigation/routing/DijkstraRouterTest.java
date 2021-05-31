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

package com.telenav.kivakit.navigation.routing;

import com.telenav.kivakit.graph.Route;
import com.telenav.kivakit.graph.Vertex;
import com.telenav.kivakit.graph.project.KivaKitGraphCoreUnitTest;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.scalars.levels.Weight;
import com.telenav.kivakit.kernel.testing.SlowTests;
import com.telenav.kivakit.map.measurements.Speed;
import com.telenav.kivakit.navigation.routing.cost.CostFunction;
import com.telenav.kivakit.navigation.routing.cost.functions.TravelTimeCostFunction;
import com.telenav.kivakit.navigation.routing.cost.functions.heuristic.RemainingDistanceToEndCostFunction;
import com.telenav.kivakit.navigation.routing.cost.functions.heuristic.SpeedCostFunction;
import com.telenav.kivakit.navigation.routing.debuggers.SwingRoutingDebugger;
import com.telenav.kivakit.navigation.routing.dijkstra.DijkstraRouter;
import com.telenav.kivakit.navigation.routing.dijkstra.DijkstraRoutingRequest;
import com.telenav.kivakit.navigation.routing.dijkstra.Direction;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author jonathanl (shibo)
 */
@Ignore
@Category({ SlowTests.class })
public class DijkstraRouterTest extends KivaKitGraphCoreUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private static RoutingDebugger DEBUGGER = RoutingDebugger.NULL;

    @SuppressWarnings("EmptyMethod")
    @Before
    public void setup()
    {
        if (DEBUG.isEnabled())
        {
            DEBUGGER = new SwingRoutingDebugger("DijkstraRouterTest");
        }
    }

    @Test
    public void testDowntownSeattleAStarRouting()
    {
        final var routes = new String[]
                {
                        "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:-671471376000001:-671471376000000:671471380000000:671471380000001:-663673433000001:-663673433000000:-286940847000001:-286940847000000:635483970000000:635483970000001:-531870054000001:-531870054000000:-537661114000001:-537661114000000:-537671576000001:-537671576000000:-193209779000001:-193209779000000:-6406066000002:-6406066000001:-6406066000000:-428246217000001:-428246217000000:-835645675000001:-835645675000000:-428246220000001:-428246220000000:-428246219000001:-428246219000000:-428246213000001:-428246213000000:6472596000000:6472596000001:6472596000002:6472596000003:6472596000004:6472596000005:6472596000006:6472596000007:6472596000008:6472596000009:620762215000000:620762215000001:620762215000002:191144465000000:191144465000001:191144465000002:191144465000003:191144465000004:532864852000000:532864852000001:532864852000002:532864852000003:532864852000004:620762217000000:620762217000001:620762217000002:679639327000000:679639327000001:8111068000000:8111068000001:8111068000002:-56178844000001:-56178844000000:-13726933000001:-13726933000000:428227905000000:428227905000001:428227905000002:428227905000003:428227871000000:428227871000001:428227887000000:428227887000001:428227882000000:428227882000001:428227877000000:428227877000001:428227893000000:428227893000001:428227850000000:428227850000001:428227899000000:428227899000001:428227865000000:428227865000001:428227818000000:428227818000001:428227854000000:428227854000001:428227838000000:428227838000001:428227833000000:428227833000001:428227822000000:428227822000001:428227842000000:428227842000001:428227828000000:428227828000001:428227860000000:428227860000001:428227803000000:428227803000001:428227847000000:428227847000001:428227808000000:428227808000001:13726934000000:13726934000001:428227851000000:428227851000001:428227812000000:428227812000001:428089354000000:428089354000001:428089350000000:428089350000001:428089352000000:428089352000001:428089351000000:428089351000001:65588069000000:65588069000001:428341218000000:428341218000001:428341218000002:332223491000000:332223491000001:332223492000000:332223492000001:332223492000002:332223492000003:332223492000004:265352909000000:265352909000001:265352909000002:265352909000003:797328240000000:797328240000001:797328240000002:797328240000003:808907363000000:808907363000001:808907363000002:808907363000003:808907363000004:758463801000000:758463801000001:758463800000000:758463800000001:428246436000000:428246436000001:-428246441000002:-428246441000001:-428246441000000:-723013876000001:-723013876000000:-723013877000001:-723013877000000",
                        "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:-671471376000001:-671471376000000:671471380000000:671471380000001:-663673433000001:-663673433000000:-286940847000001:-286940847000000:635483970000000:635483970000001:-531870054000001:-531870054000000:-537661114000001:-537661114000000:-537671576000001:-537671576000000:-193209779000001:-193209779000000:-6406066000002:-6406066000001:-6406066000000:-428246217000001:-428246217000000:-835645675000001:-835645675000000:-428246220000001:-428246220000000:-428246219000001:-428246219000000:-428246213000001:-428246213000000:6472596000000:6472596000001:6472596000002:6472596000003:6472596000004:6472596000005:6472596000006:6472596000007:6472596000008:6472596000009:620762215000000:620762215000001:620762215000002:191144465000000:191144465000001:191144465000002:191144465000003:191144465000004:532864852000000:532864852000001:532864852000002:532864852000003:532864852000004:620762217000000:620762217000001:620762217000002:679639327000000:679639327000001:8111068000000:8111068000001:8111068000002:-56178844000001:-56178844000000:-13726933000001:-13726933000000:428227905000000:428227905000001:428227905000002:428227905000003:428227871000000:428227871000001:428227887000000:428227887000001:428227882000000:428227882000001:428227877000000:428227877000001:428227893000000:428227893000001:428227850000000:428227850000001:428227899000000:428227899000001:428227865000000:428227865000001:428227818000000:428227818000001:428227854000000:428227854000001:428227838000000:428227838000001:428227833000000:428227833000001:428227822000000:428227822000001:428227842000000:428227842000001:428227828000000:428227828000001:428227860000000:428227860000001:428227803000000:428227803000001:428227847000000:428227847000001:428227808000000:428227808000001:13726934000000:13726934000001:428227851000000:428227851000001:428227812000000:428227812000001:428089354000000:428089354000001:428089350000000:428089350000001:428089352000000:428089352000001:428089351000000:428089351000001:65588069000000:65588069000001:428341218000000:428341218000001:428341218000002:332223491000000:332223491000001:332223492000000:332223492000001:332223492000002:332223492000003:332223492000004:265352909000000:265352909000001:265352909000002:265352909000003:797328240000000:797328240000001:797328240000002:797328240000003:808907363000000:808907363000001:808907363000002:808907363000003:808907363000004:758463801000000:758463801000001:758463800000000:758463800000001:428246436000000:428246436000001:-428246441000002:-428246441000001:-428246441000000:-723013876000001:-723013876000000:-723013877000001:-723013877000000",
                        "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:-671471376000001:-671471376000000:671471380000000:671471380000001:-663673433000001:-663673433000000:-286940847000001:-286940847000000:635483970000000:635483970000001:-531870054000001:-531870054000000:-537661114000001:-537661114000000:-537671576000001:-537671576000000:-193209779000001:-193209779000000:-6406066000002:-6406066000001:-6406066000000:-428246217000001:-428246217000000:-835645675000001:-835645675000000:-428246220000001:-428246220000000:-428246219000001:-428246219000000:-428246213000001:-428246213000000:6472596000000:6472596000001:6472596000002:6472596000003:6472596000004:6472596000005:6472596000006:6472596000007:6472596000008:6472596000009:620762215000000:620762215000001:620762215000002:191144465000000:191144465000001:191144465000002:191144465000003:191144465000004:532864852000000:532864852000001:532864852000002:532864852000003:532864852000004:620762217000000:620762217000001:620762217000002:679639327000000:679639327000001:8111068000000:8111068000001:8111068000002:-56178844000001:-56178844000000:-13726933000001:-13726933000000:428227905000000:428227905000001:428227905000002:428227905000003:428227871000000:428227871000001:428227887000000:428227887000001:428227882000000:428227882000001:428227877000000:428227877000001:428227893000000:428227893000001:428227850000000:428227850000001:428227899000000:428227899000001:428227865000000:428227865000001:428227818000000:428227818000001:428227854000000:428227854000001:428227838000000:428227838000001:428227833000000:428227833000001:428227822000000:428227822000001:428227842000000:428227842000001:428227828000000:428227828000001:428227860000000:428227860000001:428227803000000:428227803000001:428227847000000:428227847000001:428227808000000:428227808000001:13726934000000:13726934000001:428227851000000:428227851000001:428227812000000:428227812000001:428089354000000:428089354000001:428089350000000:428089350000001:428089352000000:428089352000001:428089351000000:428089351000001:65588069000000:65588069000001:428341218000000:428341218000001:428341218000002:332223491000000:332223491000001:332223492000000:332223492000001:332223492000002:332223492000003:332223492000004:265352909000000:265352909000001:265352909000002:265352909000003:797328240000000:797328240000001:797328240000002:797328240000003:-6447455000005:-6447455000004:-6447455000003:-6447455000002:158612542000006:158612542000007"
                };
        for (var i = 0; i < 3; i++)
        {
            final var start = osmDowntownSeattleTestEdge(6348968000003L);
            final var end = osmDowntownSeattleTestEdge(6415868000005L);
            final var weight = Weight.of(i / 3.0);
            final var route = weightedRoute(weight, start.to(), end.to(), Direction.FORWARD);
            ensureEqual(routes[i], route.toString());
        }
    }

    @Test
    public void testDowntownSeattleBackwardsRouting()
    {
        final var routes = new String[]
                {
                        "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:158943669000000:158943669000001:458060702000000:458060702000001:458060704000000:458060704000001:428246210000000:428246210000001:332223484000000:332223484000001:332223484000002:337667435000000:337667435000001:337667435000002:337667433000000:337667433000001:537696703000000:537696703000001:337667442000000:550996166000001:550996166000002:550996165000000:550996165000001:-550996169000002:-550996169000001:-550996169000000:-550996164000001:-550996164000000:-550996169000012:-550996169000011:-550996169000010:551007944000000:551007944000001:551007947000000:551007947000001:551007947000002:551007943000000:551007943000001:-551007949000001:-551007949000000:-551007940000001:-551007940000000:-551007949000009:-551007949000008:-551007949000007:-551008371000001:-551008371000000:-551008372000003:-551008372000002:-551008372000001:-551008372000000:-551008380000001:-551008380000000:-551008381000001:551008761000000:551008761000001:551008757000000:551008757000001:551008756000000:551008756000001:-551005696000004:-551005696000003:-551005696000002:551006295000000:551006295000001:551006301000000:551006301000001:551006294000000:551006294000001:-550987899000001:550988754000000:550988754000001:550988752000000:550988752000001:550988751000000:550988751000001:-550987898000000:-859953403000001:-859953403000000:-859953404000001:-859953404000000:-859953405000001:-859953405000000:-862840646000002:-862840646000001:-862840646000000:-862840647000001:-862840647000000:-659827746000004:-659827746000003:-659827746000002:-659827746000001:-659827746000000:857845352000000:857845352000001:857845355000000:857845355000001:857849458000000:857849458000001:857849458000002:857849459000000:857849459000001:857849461000000:857849461000001:857849462000000:857849462000001:-758448656000000:-758448655000001:-758448655000000:758448652000000:758448652000001:428088894000001:428088900000000:428088900000001:428088900000002:428088900000003:428088900000004:433500388000000:433500388000001:353690151000000:353690151000001:353690151000002:353690151000003:353690151000004:353690151000005:353690151000006:332223491000000:332223491000001:6438604000000:6438604000001:6438604000002:6438604000003:738637223000000:738637223000001:-165042063000001:-165042063000000:165086027000000:165086027000001:158612542000000:158612542000001:158612542000002:158612542000003:158612542000004:158612542000005:158612542000006:158612542000007",
                        "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:158943669000000:158943669000001:458060702000000:458060702000001:458060704000000:458060704000001:428246210000000:428246210000001:332223484000000:332223484000001:332223484000002:337667435000000:337667435000001:337667435000002:337667433000000:337667433000001:537696703000000:537696703000001:337667442000000:550996166000001:550996166000002:550996165000000:550996165000001:-550996169000002:-550996169000001:-550996169000000:-550996164000001:-550996164000000:-550996169000012:-550996169000011:-550996169000010:551007944000000:551007944000001:551007947000000:551007947000001:551007947000002:551007943000000:551007943000001:-551007949000001:-551007949000000:-551007940000001:-551007940000000:-551007949000009:-551007949000008:-551007949000007:-551008371000001:-551008371000000:-551008372000003:-551008372000002:-551008372000001:-551008372000000:-551008380000001:-551008380000000:-551008381000001:551008761000000:551008761000001:551008757000000:551008757000001:551008756000000:551008756000001:-551005696000004:-551005696000003:-551005696000002:551006295000000:551006295000001:551006301000000:551006301000001:551006294000000:551006294000001:-550987899000001:550988754000000:550988754000001:550988752000000:550988752000001:550988751000000:550988751000001:-550987898000000:-859953403000001:-859953403000000:-859953404000001:-859953404000000:-859953405000001:-859953405000000:-862840646000002:-862840646000001:-862840646000000:-862840647000001:-862840647000000:-659827746000004:-659827746000003:-659827746000002:-659827746000001:-659827746000000:857845352000000:857845352000001:857845355000000:857845355000001:857849458000000:857849458000001:857849458000002:857849459000000:857849459000001:857849461000000:857849461000001:857849462000000:857849462000001:-758448656000000:-758448655000001:-758448655000000:758448652000000:758448652000001:428088894000001:428088900000000:428088900000001:428088900000002:428088900000003:428088900000004:433500388000000:433500388000001:353690151000000:353690151000001:353690151000002:353690151000003:353690151000004:353690151000005:353690151000006:332223491000000:332223491000001:6438604000000:6438604000001:6438604000002:6438604000003:738637223000000:738637223000001:-165042063000001:-165042063000000:165086027000000:165086027000001:158612542000000:158612542000001:158612542000002:158612542000003:158612542000004:158612542000005:158612542000006:158612542000007",
                        "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:158943669000000:158943669000001:458060702000000:458060702000001:458060704000000:458060704000001:428246210000000:428246210000001:332223484000000:332223484000001:332223484000002:337667435000000:337667435000001:337667435000002:337667433000000:337667433000001:537696703000000:537696703000001:337667442000000:550996166000001:550996166000002:550996165000000:550996165000001:-550996169000002:-550996169000001:-550996169000000:-550996164000001:-550996164000000:-550996169000012:-550996169000011:-550996169000010:551007944000000:551007944000001:551007947000000:551007947000001:551007947000002:551007943000000:551007943000001:-551007949000001:-551007949000000:-551007940000001:-551007940000000:-551007949000009:-551007949000008:-551007949000007:-551008371000001:-551008371000000:-551008372000003:-551008372000002:-551008372000001:-551008372000000:-551008380000001:-551008380000000:-551008381000001:551008761000000:551008761000001:551008757000000:551008757000001:551008756000000:551008756000001:-551005696000004:-551005696000003:-551005696000002:551006295000000:551006295000001:551006301000000:551006301000001:551006294000000:551006294000001:-550987899000001:550988754000000:550988754000001:550988752000000:550988752000001:550988751000000:550988751000001:-550987898000000:-859953403000001:-859953403000000:-859953404000001:-859953404000000:-859953405000001:-859953405000000:-862840646000002:-862840646000001:-862840646000000:-862840647000001:-862840647000000:-659827746000004:-659827746000003:-659827746000002:-659827746000001:-659827746000000:857845352000000:857845352000001:857845355000000:857845355000001:857849458000000:857849458000001:857849458000002:857849459000000:857849459000001:857849461000000:857849461000001:857849462000000:857849462000001:-758448656000000:-758448655000001:-758448655000000:758448652000000:758448652000001:428088894000001:428088900000000:428088900000001:428088900000002:428088900000003:428088900000004:433500388000000:433500388000001:353690151000000:353690151000001:353690151000002:353690151000003:353690151000004:353690151000005:353690151000006:332223491000000:332223491000001:6438604000000:6438604000001:6438604000002:6438604000003:738637223000000:738637223000001:-165042063000001:-165042063000000:165086027000000:165086027000001:158612542000000:158612542000001:158612542000002:158612542000003:158612542000004:158612542000005:158612542000006:158612542000007"
                };
        for (var i = 0; i < 3; i++)
        {
            final var start = osmDowntownSeattleTestEdge(6348968000003L);
            final var end = osmDowntownSeattleTestEdge(6415868000005L);
            final var route = fixedSpeedRoute(start.to(), end.to(), Direction.BACKWARD);
            ensureEqual(routes[i], route.toString());
        }
    }

    @Test
    public void testDowntownSeattleTravelTimeRouting()
    {
        final var start = osmDowntownSeattleTestEdge(6348968000003L);
        final var end = osmDowntownSeattleTestEdge(6415868000005L);
        final var route = fixedSpeedRoute(start.to(), end.to(), Direction.FORWARD);
        final var expected = "6348968000004:427915135000000:427915135000001:427915134000000:427915134000001:715347409000000:715347409000001:427914314000000:427914314000001:427914145000000:427914145000001:427914145000002:428244920000000:428244920000001:109918321000000:109918321000001:428244925000000:428244925000001:428244926000000:428244926000001:533331378000000:533331378000001:533331377000000:533331377000001:533331376000000:533331376000001:428244921000000:428244921000001:533331375000000:533331375000001:533331374000000:533331374000001:428244927000000:428244927000001:428244922000000:428244922000001:428244923000000:428244923000001:109918322000000:109918322000001:158943669000000:158943669000001:458060702000000:458060702000001:458060704000000:458060704000001:428246210000000:428246210000001:332223484000000:332223484000001:332223484000002:337667435000000:337667435000001:337667435000002:337667433000000:337667433000001:537696703000000:537696703000001:337667442000000:550996166000001:550996166000002:550996165000000:550996165000001:-550996169000002:-550996169000001:-550996169000000:-550996164000001:-550996164000000:-550996169000012:-550996169000011:-550996169000010:551007944000000:551007944000001:551007947000000:551007947000001:551007947000002:551007943000000:551007943000001:-551007949000001:-551007949000000:-551007940000001:-551007940000000:-551007949000009:-551007949000008:-551007949000007:-551008371000001:-551008371000000:-551008372000003:-551008372000002:-551008372000001:-551008372000000:-551008380000001:-551008380000000:-551008381000001:551008761000000:551008761000001:551008757000000:551008757000001:551008756000000:551008756000001:-551005696000004:-551005696000003:-551005696000002:551006295000000:551006295000001:551006301000000:551006301000001:551006294000000:551006294000001:-550987899000001:550988754000000:550988754000001:550988752000000:550988752000001:550988751000000:550988751000001:-550987898000000:-859953403000001:-859953403000000:-859953404000001:-859953404000000:-859953405000001:-859953405000000:-862840646000002:-862840646000001:-862840646000000:-862840647000001:-862840647000000:-659827746000004:-659827746000003:-659827746000002:-659827746000001:-659827746000000:857845352000000:857845352000001:857845355000000:857845355000001:857849458000000:857849458000001:857849458000002:857849459000000:857849459000001:857849461000000:857849461000001:857849462000000:857849462000001:-758448656000000:-758448655000001:-758448655000000:758448652000000:758448652000001:428088894000001:428088900000000:428088900000001:428088900000002:428088900000003:428088900000004:433500388000000:433500388000001:353690151000000:353690151000001:353690151000002:353690151000003:353690151000004:353690151000005:353690151000006:332223491000000:332223491000001:6438604000000:6438604000001:6438604000002:6438604000003:738637223000000:738637223000001:-165042063000001:-165042063000000:165086027000000:165086027000001:158612542000000:158612542000001:158612542000002:158612542000003:158612542000004:158612542000005:158612542000006:158612542000007";
        ensureEqual(route.toString(), expected);
    }

    @Test
    public void testGreenLakeAStarRouting()
    {
        final var start = osmGreenLakeSeattleEdge(6366507000001L);
        final var end = osmGreenLakeSeattleEdge(4794181000017L);
        final var weight = Weight.of(99 / 100.0);
        final var route = weightedRoute(weight, start.to(), end.from(), Direction.FORWARD);
        final var expected = "-6366507000001:6426970000003:6426970000004:6426970000005:263563169000000:263563169000001:263563169000002:263563169000003:263563169000004:263563169000005:263563169000006:263563169000007:263563169000008:263563169000009:263563169000010:263563169000011:263563169000012:-457542644000001:-457542644000000:6426947000000:6426947000001:263563232000000:263563232000001:457542643000000:457542643000001:457542643000002:457542643000003:457542643000004:457542642000000:457542642000001:428221343000000:428221343000001:428221344000000:428221344000001:6410233000000:6410233000001:6410233000002:6410233000003:6410233000004:6410233000005:6410233000006:6410233000007:6410233000008:6410233000009:6410233000010:6410233000011:6410233000012:6410233000013:6410233000014:6410233000015:6410233000016:6410233000017:6410233000018:6410233000019:6410233000020:428091416000000:428091416000001:31939714000000:31939714000001:351967840000000:351967840000001:351967839000000:351967839000001:428341321000000:428341321000001:274475670000000:274475670000001:274475670000002:428341319000000:428341319000001:289314033000000:289314033000001:428341320000000:428341320000001:332752098000000:332752098000001:332752098000002:332752098000003:332752098000004:332752098000005:428083349000000:428083349000001:332752097000000:332752097000001:336155509000000:336155509000001:336155509000002:336155509000003:428082940000000:428082940000001:-428083351000001:-428083351000000:-635971819000001:-635971819000000:-428083352000001:-428083352000000:-4794181000017";
        ensureEqual(route.toString(), expected);
    }

    @Test
    public void testGreenLakeTravelTimeRouting()
    {
        final var start = osmGreenLakeSeattleEdge(6366507000001L);
        final var end = osmGreenLakeSeattleEdge(4794181000017L);
        final var route = fixedSpeedRoute(start.to(), end.from(), Direction.FORWARD);
        final var expected = "-6366507000001:6426970000003:6426970000004:6426970000005:263563169000000:263563169000001:263563169000002:263563169000003:263563169000004:263563169000005:263563169000006:263563169000007:263563169000008:263563169000009:263563169000010:263563169000011:263563169000012:-457542644000001:-457542644000000:6426947000000:6426947000001:263563232000000:263563232000001:-517229875000002:-517229875000001:-517229875000000:-537606390000002:-241540263000001:-241540263000000:159492554000000:159492554000001:159492554000002:426221362000004:426221362000005:426221362000006:426221362000007:426221362000008:426221362000009:426221362000010:426221362000011:426221362000012:426221362000013:426221362000014:426221362000015:426221362000016:241539655000000:241539655000001:426221362000021:426221362000022:426221362000023:52324874000000:52324874000001:35394664000001:35394664000002:35394664000003:6410233000020:428091416000000:428091416000001:31939714000000:31939714000001:351967840000000:351967840000001:351967839000000:351967839000001:428341321000000:428341321000001:274475670000000:274475670000001:274475670000002:428341319000000:428341319000001:289314033000000:289314033000001:428341320000000:428341320000001:332752098000000:332752098000001:6381214000005:6381214000006:6381214000007:6381214000008:6381214000009:6381214000010:6381214000011:6381214000012:6381214000013:4794181000015:4794181000016";
        ensureEqual(route.toString(), expected);
    }

    @Test
    public void testOneEdgeRouting()
    {
        final var edge = osmGreenLakeSeattleEdge(6366507000001L);
        ensure(edge.isTwoWay());
        ensureEqual(route(edge), fixedSpeedRoute(edge.from(), edge.to(), Direction.FORWARD));
        ensureEqual(route(edge.reversed()), fixedSpeedRoute(edge.to(), edge.from(), Direction.FORWARD));
    }

    private Route fixedSpeedRoute(final Vertex start, final Vertex end, final Direction direction)
    {
        return route(start, end, direction, new SpeedCostFunction(), null);
    }

    private Route route(final Vertex start, final Vertex end, final Direction direction,
                        final CostFunction costFunction,
                        final CostFunction heuristicCostFunction)
    {
        final RoutingRequest request = new DijkstraRoutingRequest(start, end)
                .withDebugger(DEBUGGER)
                .withDirection(direction);

        final var routeResult = new DijkstraRouter(costFunction)
                .withHeuristicCostFunction(heuristicCostFunction)
                .findRoute(request);

        DEBUG.trace("Found route ${debug}", routeResult);
        return routeResult.route();
    }

    @SuppressWarnings("SameParameterValue")
    private Route weightedRoute(final Weight weight, final Vertex start, final Vertex end, final Direction direction)
    {
        return route(start, end, direction,
                new TravelTimeCostFunction(Speed.HIGHWAY_SPEED, start, end),
                new RemainingDistanceToEndCostFunction(start.location(), end.location())
                        .weightedSum(weight, new SpeedCostFunction()));
    }
}
