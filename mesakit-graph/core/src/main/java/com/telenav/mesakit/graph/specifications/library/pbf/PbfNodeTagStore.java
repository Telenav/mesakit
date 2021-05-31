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

package com.telenav.mesakit.graph.specifications.library.pbf;

import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.specifications.common.element.store.TagStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.GraphNodeIndex;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfTagCodec;

public class PbfNodeTagStore
{
    private final TagStore tags;

    private int index = 1;

    public PbfNodeTagStore(final PbfTagCodec codec)
    {
        tags = new TagStore("PbfNodeTagStore.tags", codec);
    }

    public void add(final PbfTagList tags)
    {
        this.tags.set(new GraphNodeIndex(index++), tags);
    }

    public PbfTagList get(final GraphElement element)
    {
        return tags.tagList(element);
    }
}
