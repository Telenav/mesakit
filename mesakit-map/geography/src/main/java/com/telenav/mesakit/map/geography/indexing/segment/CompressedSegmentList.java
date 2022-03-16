////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.geography.indexing.segment;

import com.telenav.mesakit.map.geography.shape.segment.Segment;

import java.util.AbstractList;
import java.util.Collection;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

public final class CompressedSegmentList extends AbstractList<Segment>
{
    private long[] start;

    private long[] end;

    private int size;

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean addAll(Collection<? extends Segment> segments)
    {
        if (start != null || end != null)
        {
            fail("Only supports one call to addAll()");
            return false;
        }
        size = segments.size();
        start = new long[size];
        end = new long[size];
        var index = 0;
        for (Segment segment : segments)
        {
            start[index] = segment.start().asLong();
            end[index] = segment.end().asLong();
            index++;
        }
        return true;
    }

    @Override
    public Segment get(int index)
    {
        return new Segment(start[index], end[index]);
    }

    @Override
    public int size()
    {
        return size;
    }
}
