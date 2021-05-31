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

package com.telenav.mesakit.graph.specifications.common.place;

import com.telenav.kivakit.kernel.data.comparison.Differences;
import com.telenav.mesakit.graph.Place;

public class PlaceDifferences extends Differences
{
    private final Place a;

    private final Place b;

    public PlaceDifferences(final Place a, final Place b)
    {
        this.a = a;
        this.b = b;
    }

    public Differences compare()
    {
        final var differences = new Differences();

        // Add differences that are meaningful for all data specifications
        differences.compare("name", a.name(), b.name());
        differences.compare("population", a.population(), b.population());
        differences.compare("location", a.location(), b.location());
        differences.compare("type", a.type(), b.type());

        return differences;
    }
}
