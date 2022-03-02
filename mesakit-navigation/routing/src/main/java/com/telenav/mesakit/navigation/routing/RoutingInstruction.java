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

import com.telenav.kivakit.core.messaging.Message;

/**
 * @author jonathanl (shibo)
 */
public class RoutingInstruction
{
    /** The edge should be explored */
    public static final RoutingInstruction EXPLORE_EDGE = new RoutingInstruction(Meaning.EXPLORE_EDGE, "Explore Edge");

    /** The edge should be ignored */
    public static final RoutingInstruction IGNORE_EDGE = new RoutingInstruction(Meaning.IGNORE_EDGE, "Ignore Edge");

    public enum Meaning
    {
        EXPLORE_EDGE,
        IGNORE_EDGE,
        STOP_ROUTING
    }

    private final String message;

    private final Meaning meaning;

    public RoutingInstruction(Meaning meaning, String message, Object... arguments)
    {
        this.meaning = meaning;
        this.message = Message.format(message, arguments);
    }

    public Meaning meaning()
    {
        return meaning;
    }

    public String message()
    {
        return message;
    }
}
