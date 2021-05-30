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

package com.telenav.tdk.graph.traffic.roadsection.codings.legacy;

import com.telenav.tdk.core.kernel.conversion.string.BaseStringConverter;
import com.telenav.tdk.core.kernel.language.string.StringList;
import com.telenav.tdk.core.kernel.messaging.Listener;
import com.telenav.tdk.core.kernel.messaging.Message;
import com.telenav.tdk.core.kernel.time.Frequency;
import com.telenav.tdk.graph.traffic.MapData;
import com.telenav.tdk.graph.traffic.roadsection.RoadSectionCode;
import com.telenav.tdk.graph.traffic.roadsection.codings.osm.PbfRoadSectionCode;
import com.telenav.tdk.graph.traffic.roadsection.codings.telenav.TelenavTrafficLocationCode;
import com.telenav.tdk.graph.traffic.roadsection.codings.tmc.TmcCode;
import com.telenav.tdk.graph.traffic.roadsection.codings.tomtom.TomTomRoadSectionCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses legacy traffic id format, which may be one of the following format:
 *
 * <pre>
 * 1. a single tmc-id, e.g. '106-04640'
 *
 * 2. a single edge-id, e.g. '2668594'
 *
 * 3. combination of tmc-id and edge-id (mfid): [forward-tmc-id]:[backward-tmc-id]:[edge-id[%edge-id]]
 *    e.g. 129-04658::26685941%723695504%761217275
 *    for this case, we only need to extract forward-tmc-id and edge ids, and ignore backward-tmc-id
 * </pre>
 *
 * @author jonathanl (shibo)
 */
public class LegacyRoadSectionCodeConverter extends BaseStringConverter<List<RoadSectionCode>>
{
    private final TomTomRoadSectionCode.Converter tomtomEdgeIdentifierConverter = new TomTomRoadSectionCode.Converter(
            this)
    {
        @Override
        protected Frequency problemBroadcastFrequency()
        {
            return Frequency.EVERY_MINUTE;
        }
    };

    private final PbfRoadSectionCode.Converter osmEdgeIdentifierConverter = new PbfRoadSectionCode.Converter(this)
    {
        @Override
        protected Frequency problemBroadcastFrequency()
        {
            return Frequency.EVERY_MINUTE;
        }
    };

    private final TmcCode.Converter tmcCodeConverter = new TmcCode.Converter(this)
    {
        @Override
        protected Frequency problemBroadcastFrequency()
        {
            return Frequency.EVERY_MINUTE;
        }
    };

    private final TelenavTrafficLocationCode.Converter telenavTrafficLocationCodeConverter = new TelenavTrafficLocationCode.Converter(
            this)
    {
        @Override
        protected Frequency problemBroadcastFrequency()
        {
            return Frequency.EVERY_MINUTE;
        }
    };

    private final MapData mapData;

    public LegacyRoadSectionCodeConverter(final Listener<Message> listener, final MapData mapData)
    {
        super(listener);
        this.mapData = mapData;
        this.tomtomEdgeIdentifierConverter.allowEmpty(true);
        this.tomtomEdgeIdentifierConverter.allowNull(true);
        this.osmEdgeIdentifierConverter.allowEmpty(true);
        this.osmEdgeIdentifierConverter.allowNull(true);
        this.tmcCodeConverter.allowEmpty(true);
        this.tmcCodeConverter.allowNull(true);
        this.telenavTrafficLocationCodeConverter.allowEmpty(true);
        this.telenavTrafficLocationCodeConverter.allowNull(true);
    }

    protected void addOsmEdgeIdentifier(final String edge, final List<RoadSectionCode> codes)
    {
        final var identifier = this.osmEdgeIdentifierConverter.convert(edge.trim());
        if (identifier != null)
        {
            codes.add(identifier);
        }
    }

    protected void addTelenavTrafficLocationCode(final String code, final List<RoadSectionCode> codes)
    {
        final var identifier = this.telenavTrafficLocationCodeConverter.convert(code.trim());
        if (identifier != null)
        {
            codes.add(identifier);
        }
    }

    protected void addTmcCode(final String codeString, final List<RoadSectionCode> codes)
    {
        final var code = this.tmcCodeConverter.convert(codeString.trim());
        if (code != null)
        {
            codes.add(code);
        }
    }

    protected void addTomTomEdgeIdentifier(final String edge, final List<RoadSectionCode> codes)
    {
        final var identifier = this.tomtomEdgeIdentifierConverter.convert(edge.trim());
        if (identifier != null)
        {
            codes.add(identifier);
        }
    }

    /**
     * Parses legacy traffic id format, which may be one of the following format:
     *
     * <pre>
     * 1. a single tmc-id, e.g. '106-04640'
     *
     * 2. a single edge-id, e.g. '2668594'
     *
     * 3. combination of tmc-id and edge-id (mfid format): [forward-tmc-id]:[backward-tmc-id]:[edge-id[%edge-id]]
     *    e.g. 129-04658:129+04658:26685941%723695504%761217275
     *    for this case, we only need to extract forward-tmc-id and edge ids, but ignore backward-tmc-id
     * </pre>
     */
    @Override
    protected List<RoadSectionCode> onConvertToObject(final String value)
    {
        final List<RoadSectionCode> codes = new ArrayList<>();
        final var chunks = StringList.split(value, ":");
        var firstChunk = chunks.get(0).trim();

        // if it is a mfid (chunks size > 1) or a single tmc, the first chunk must be tmc code
        if (this.mapData.equals(MapData.OSM))
        {
            addTelenavTrafficLocationCode(firstChunk, codes);
            if (chunks.size() > 2)
            {
                // extract each edge id from the third chunk
                for (final var edge : chunks.get(2).split("%"))
                {
                    addOsmEdgeIdentifier(edge, codes);
                }
            }
        }
        else
        {
            // This cleans up the TMC id if it starts with + or -, by removing the characters
            // appropriately. Sometimes the map data is compiled with the leading + & - characters
            // which are made part of the request. We are filtering such values.
            if (firstChunk.startsWith("+") || firstChunk.startsWith("-"))
            {
                firstChunk = firstChunk.substring(1);
            }

            if (TmcCode.isTmcCode(firstChunk))
            {
                addTmcCode(firstChunk, codes);
            }
            // else, it is a single edge id, then the first chunk is edge id
            else
            {
                addTomTomEdgeIdentifier(firstChunk, codes);
            }

            // if it is mfid and contains edge ids
            if (chunks.size() > 2)
            {
                // extract each edge id from the third chunk
                for (final var edge : chunks.get(2).split("%"))
                {
                    addTomTomEdgeIdentifier(edge, codes);
                }
            }
        }
        return codes;
    }
}
