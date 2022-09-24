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

import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramSpatialIndex;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;

import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

@UmlClassDiagram(diagram = DiagramSpatialIndex.class)
public class SegmentRTreeSpatialIndex extends RTreeSpatialIndex<Segment>
{
    public class CompressedLeaf extends Leaf<Segment>
    {
        private final CompressedSegmentList segments;

        CompressedLeaf(InteriorNode<Segment> parent)
        {
            super(SegmentRTreeSpatialIndex.this, parent);
            segments = new CompressedSegmentList();
        }

        @Override
        public void addAll(List<Segment> segments)
        {
            this.segments.addAll(segments);
            bounds(Rectangle.fromBoundedObjects(segments));
        }

        @Override
        protected void addElement(Segment element)
        {
            unsupported();
        }

        @Override
        protected List<Segment> elements()
        {
            return segments;
        }

        @Override
        protected int size()
        {
            return segments.size();
        }
    }

    public SegmentRTreeSpatialIndex(String objectName,
                                    Maximum segmentCount,
                                    Iterable<Segment> segments)
    {
        super(objectName, new RTreeSettings());
        bulkLoad(new ObjectList<Segment>(segmentCount).appendThen(segments));
    }

    @Override
    public Leaf<Segment> newLeaf(InteriorNode<Segment> parent)
    {
        return new CompressedLeaf(parent);
    }
}
