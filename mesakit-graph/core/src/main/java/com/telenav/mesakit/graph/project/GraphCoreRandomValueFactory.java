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

package com.telenav.mesakit.graph.project;

import com.telenav.mesakit.map.geography.project.MapGeographyRandomValueFactory;
import com.telenav.mesakit.map.road.model.BetweenCrossRoads;
import com.telenav.mesakit.map.road.model.Intersection;
import com.telenav.mesakit.map.road.model.RoadName;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to create random values. This is used most frequently in testing.
 *
 * @author jonathanl (shibo)
 */
public class GraphCoreRandomValueFactory extends MapGeographyRandomValueFactory
{
    protected abstract class RandomListBuilder<T>
    {
        final List<T> list = new ArrayList<>();

        protected RandomListBuilder(final int minimum, final int maximum)
        {
            for (var i = 0; i < newInt(minimum, maximum); i++)
            {
                this.list.add(newElement());
            }
        }

        public List<T> list()
        {
            return this.list;
        }

        protected abstract T newElement();
    }

    public GraphCoreRandomValueFactory()
    {
    }

    public GraphCoreRandomValueFactory(final long seed)
    {
        super(seed);
    }

    public BetweenCrossRoads newBetweenCrossStreets()
    {
        return BetweenCrossRoads.newInstance(newStreetName(), newStreetName(), newStreetName());
    }

    public Intersection newIntersection()
    {
        return Intersection.forStreets(newStreetName(), newStreetName());
    }

    public RoadName newStreetName()
    {
        return RoadName.forName(newString());
    }
}