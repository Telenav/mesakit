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

package com.telenav.mesakit.graph.analytics.classification.classifiers.turn;

import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.RouteBuilder;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.project.KivaKitGraphCoreUnitTest;
import org.junit.Before;
import org.junit.Ignore;

/**
 * @author jonathanl (shibo)
 */
@Ignore
public class ComplexTurnClassifierTest extends KivaKitGraphCoreUnitTest
{
    private static Graph huronCharterGraph;

    private static TurnClassifier classifier;

    @Before
    public void initialize()
    {
        if (huronCharterGraph == null)
        {
            huronCharterGraph = osmHuronCharter();
        }
        if (classifier == null)
        {
            classifier = ComplexTurnClassifier.DEFAULT;
        }
    }

    /**
     * 42.137236, -83.382675
     */
    public void testUTurn2()
    {
        final Route route = huronCharterRoute(0L);
        final TurnType turnType = classifier.type(route);
        ensureEqual(TurnType.LEFT_SIDE_U_TURN, turnType);
    }

    private Route huronCharterRoute(final Long... edges)
    {
        final RouteBuilder builder = new RouteBuilder();
        for (final Long edge : edges)
        {
            builder.append(huronCharterGraph.edgeForIdentifier(new EdgeIdentifier(edge)));
        }
        return builder.route();
    }
}
