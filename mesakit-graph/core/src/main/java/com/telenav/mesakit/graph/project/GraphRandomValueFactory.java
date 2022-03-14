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

import com.telenav.mesakit.map.geography.GeographyRandomValueFactory;
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
public class GraphRandomValueFactory extends GeographyRandomValueFactory
{
    protected abstract class RandomListBuilder<T>
    {
        final List<T> list = new ArrayList<>();

        protected RandomListBuilder(int minimum, int maximum)
        {
            for (var i = 0; i < newInt(minimum, maximum); i++)
            {
                list.add(newElement());
            }
        }

        public List<T> list()
        {
            return list;
        }

        protected abstract T newElement();
    }

    public GraphRandomValueFactory()
    {
    }

    public GraphRandomValueFactory(long seed)
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
