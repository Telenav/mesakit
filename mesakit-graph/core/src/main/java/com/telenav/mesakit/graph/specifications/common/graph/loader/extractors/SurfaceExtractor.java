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
import com.telenav.mesakit.map.road.model.RoadSurface;

import java.util.HashMap;
import java.util.Map;

public class SurfaceExtractor extends BaseExtractor<RoadSurface, PbfWay>
{
    private static final Map<String, RoadSurface> surfaceForName = new HashMap<>();

    static
    {
        surfaceForName.put("paved", RoadSurface.PAVED);
        surfaceForName.put("cobblestone", RoadSurface.PAVED);
        surfaceForName.put("asphalt", RoadSurface.PAVED);
        surfaceForName.put("concrete", RoadSurface.PAVED);
        surfaceForName.put("unpaved", RoadSurface.UNPAVED);
        surfaceForName.put("dirt", RoadSurface.UNPAVED);
        surfaceForName.put("grass", RoadSurface.UNPAVED);
        surfaceForName.put("mud", RoadSurface.UNPAVED);
        surfaceForName.put("earth", RoadSurface.UNPAVED);
        surfaceForName.put("sand", RoadSurface.UNPAVED);
    }

    public SurfaceExtractor(final Listener listener)
    {
        super(listener);
    }

    @Override
    public RoadSurface onExtract(final PbfWay way)
    {
        final var surfaceName = way.tagValue("surface");
        final var surface = surfaceForName.get(surfaceName);
        if (surface != null)
        {
            return surface;
        }
        return surfaceName == null ? RoadSurface.NOT_APPLICABLE : RoadSurface.POOR_CONDITION;
    }
}
