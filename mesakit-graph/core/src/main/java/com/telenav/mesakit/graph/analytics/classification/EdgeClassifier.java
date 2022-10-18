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

package com.telenav.mesakit.graph.analytics.classification;

import com.telenav.mesakit.graph.Edge;

/**
 * Accepts or rejects an edge as part of some classification scheme. There are other interfaces that could be used, but
 * this class makes the usage clear.
 *
 * @author jonathanl (shibo)
 */
@FunctionalInterface
public interface EdgeClassifier
{
    /**
     * Returns true if the given edge is accepted in some classification scheme
     */
    boolean accept(Edge edge);
}
