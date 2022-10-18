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

package com.telenav.mesakit.navigation.routing.cost;

import com.telenav.mesakit.graph.Route;

/**
 * Function for determining if a given route is allowed
 *
 * @author jonathanl (shibo)
 */
public interface RoutePermissionFunction
{
    RoutePermissionFunction NULL = route -> true;

    /**
     * Returns true if the given route is allowed
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean allowed(Route route);
}
