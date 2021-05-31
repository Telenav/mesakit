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

package com.telenav.mesakit.graph.traffic.extractors;

import com.telenav.kivakit.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.ArrayList;
import java.util.List;

/**
 * Extract traffic identifiers from a {@link Way}
 *
 * @author matthieun
 */
public class TrafficIdentifiersExtractor extends BaseExtractor<TrafficIdentifiersExtractor.RoadSectionIdentifierExtract, PbfWay>
{
    /**
     * @author matthieun
     */
    public static class RoadSectionIdentifierExtract
    {
        private final Iterable<RoadSectionIdentifier> forward;

        private final Iterable<RoadSectionIdentifier> reverse;

        public RoadSectionIdentifierExtract(final Iterable<RoadSectionIdentifier> forward,
                                            final Iterable<RoadSectionIdentifier> reverse)
        {
            this.forward = forward;
            this.reverse = reverse;
        }

        public Iterable<RoadSectionIdentifier> forward()
        {
            return forward;
        }

        public boolean hasForward()
        {
            return Iterables.size(forward) > 0;
        }

        public boolean hasReverse()
        {
            return Iterables.size(reverse) > 0;
        }

        public Iterable<RoadSectionIdentifier> reverse()
        {
            return reverse;
        }
    }

    public TrafficIdentifiersExtractor(final Listener listener)
    {
        super(listener);
    }

    @Override
    public RoadSectionIdentifierExtract onExtract(final PbfWay way)
    {
        final var traffic = way.tagValue("tmcid");
        final List<RoadSectionIdentifier> forward = new ArrayList<>();
        final List<RoadSectionIdentifier> reverse = new ArrayList<>();
        final var oneway = way.tagValue("oneway");
        final var isReversedOneWay = "-1".equals(oneway);

        if (traffic != null && !traffic.isEmpty())
        {
            final var tmcs = StringList.split(traffic, "|");
            for (final var tmc : tmcs)
            {
                final var identifier = TmcCode.forCode(tmc.substring(1).toUpperCase()).asIdentifier(false);

                if (isReversedOneWay)
                {
                    if (tmc.startsWith("-"))
                    {
                        forward.add(identifier.reversed());
                        break;
                    }
                }
                else
                {
                    if (tmc.startsWith("+"))
                    {
                        forward.add(identifier);
                    }
                    else
                    {
                        reverse.add(identifier.reversed());
                    }
                }
            }
            return new RoadSectionIdentifierExtract(forward, reverse);
        }
        return null;
    }
}
