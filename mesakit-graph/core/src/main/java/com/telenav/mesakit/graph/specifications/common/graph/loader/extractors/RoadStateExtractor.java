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

package com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.road.model.RoadState;

public class RoadStateExtractor extends BaseExtractor<RoadStateExtractor.ExtractedRoadState, PbfWay>
{
    public static class ExtractedRoadState
    {
        private RoadState state;

        private boolean reversed;

        public ExtractedRoadState(final RoadState state)
        {
            this.state = state;
        }

        public boolean isReversed()
        {
            return reversed;
        }

        public RoadState state()
        {
            return state;
        }

        @SuppressWarnings("UnusedReturnValue")
        public ExtractedRoadState state(final RoadState state)
        {
            this.state = state;
            return this;
        }

        ExtractedRoadState reversed(final boolean reversed)
        {
            this.reversed = reversed;
            return this;
        }
    }

    public RoadStateExtractor(final Listener listener)
    {
        super(listener);
    }

    @Override
    public ExtractedRoadState onExtract(final PbfWay way)
    {
        final var oneway = way.tagValue("oneway");
        final var highway = way.tagValue("highway");
        final var reversed = "-1".equals(oneway) || "reversed".equals(oneway);
        if (way.tagValueIs("junction", "roundabout"))
        {
            return new ExtractedRoadState(RoadState.ONE_WAY).reversed(reversed);
        }
        if (oneway == null)
        {
            if ("motorway".equals(highway) || "motorway_link".equals(highway))
            {
                return new ExtractedRoadState(RoadState.ONE_WAY);
            }
            else
            {
                return new ExtractedRoadState(RoadState.TWO_WAY);
            }
        }
        if ("reversible".equals(oneway))
        {
            return new ExtractedRoadState(RoadState.TWO_WAY);
        }
        if ("yes".equals(oneway) || "true".equals(oneway) || "1".equals(oneway))
        {
            return new ExtractedRoadState(RoadState.ONE_WAY);
        }
        if ("no".equals(oneway) || "false".equals(oneway) || "0".equals(oneway) || "alternate".equals(oneway) || "alternating".equals(oneway))
        {
            return new ExtractedRoadState(RoadState.TWO_WAY);
        }
        if (reversed)
        {
            return new ExtractedRoadState(RoadState.ONE_WAY).reversed(true);
        }
        return new ExtractedRoadState(RoadState.NULL);
    }
}
