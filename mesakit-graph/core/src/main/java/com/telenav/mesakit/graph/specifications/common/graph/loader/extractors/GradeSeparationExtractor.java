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

import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.road.model.GradeSeparation;

public class GradeSeparationExtractor extends BaseExtractor<GradeSeparation, PbfWay>
{
    public enum Type
    {
        FROM,
        TO
    }

    private final Type type;

    public GradeSeparationExtractor(Listener listener, Type type)
    {
        super(listener);
        this.type = type;
    }

    @Override
    public GradeSeparation onExtract(PbfWay tags)
    {
        var level = tags.tagValueAsNaturalNumber(type == Type.FROM ? "zlevel:ref" : "zlevel:nonref");
        return level == Ints.INVALID_INT ? GradeSeparation.GROUND : GradeSeparation.of(level);
    }
}
