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

package com.telenav.mesakit.navigation.routing;

import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.navigation.routing.cost.Cost;

/**
 * Request for a route
 *
 * @author jonathanl (shibo)
 */
public abstract class RoutingRequest
{
    /** Start time of routing */
    private Time startTime;

    /** Start vertex for route (origin) */
    private final Vertex start;

    /** End vertex for route (destination) */
    private final Vertex end;

    /** Routing effort limiter */
    private RoutingLimiter limiter = RoutingLimiter.UNLIMITED;

    /** Debugger to use if any */
    private RoutingDebugger debugger = RoutingDebugger.NULL;

    protected RoutingRequest(Vertex start, Vertex end)
    {
        this.start = start;
        this.end = end;
    }

    protected RoutingRequest(RoutingRequest that, RoutingLimiter limiter, RoutingDebugger debugger)
    {
        start = that.start;
        end = that.end;
        if (that.limiter != null)
        {
            this.limiter = that.limiter;
        }
        if (that.debugger != null)
        {
            this.debugger = that.debugger;
        }
        if (limiter != null)
        {
            this.limiter = limiter;
        }
        if (debugger != null)
        {
            this.debugger = debugger;
        }
    }

    public RoutingDebugger debugger()
    {
        return debugger;
    }

    public abstract String description();

    public Distance distance()
    {
        return start().location().distanceTo(end().location());
    }

    public Vertex end()
    {
        return end;
    }

    public boolean isDebugging()
    {
        return debugger != RoutingDebugger.NULL;
    }

    public boolean isEnd(Vertex vertex)
    {
        return vertex.equals(end());
    }

    public RoutingLimiter limiter()
    {
        return limiter;
    }

    public void onRelaxed(Route route, Cost cost)
    {
        debugger.onRelaxed(route, cost);
    }

    public void onStartRouting()
    {
        // Initialize limiter
        limiter.start(this);

        // Start time of routing
        startTime = Time.now();

        // Tell the debugger we're starting
        debugger.onStart(this);
    }

    public Vertex start()
    {
        return start;
    }

    @Override
    public String toString()
    {
        return "[RoutingRequest start = " + start +
                ", end = " + end +
                ", limiter = " + limiter + "]";
    }

    protected Duration elapsed()
    {
        return startTime.elapsedSince();
    }

    protected void onEndRouting(RoutingResponse response)
    {
        debugger.onEnd(this, response);
    }

    protected void onSettled(Vertex vertex, Cost cost)
    {
        debugger.onSettled(vertex, cost);
    }
}
