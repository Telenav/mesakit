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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramPolyline;

/**
 * Represents a section of a polyline
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolylineSection
{
    /** The shape of the parent polyline */
    private final Polyline parent;

    /** The 'from' index in the parent polyline */
    private final int fromIndex;

    /** The 'to' index in the parent polyline */
    private final int toIndex;

    /** Keep the size to validate that the parent didn't change later */
    private final int parentSize;

    public PolylineSection(Polyline parent, int fromIndex, int toIndex)
    {
        assert parent != null;
        assert (parent.size() >= 2);
        assert (fromIndex >= 0);
        assert (fromIndex < parent.size());
        assert (toIndex >= 0);
        assert (toIndex < parent.size());
        assert (fromIndex < toIndex);

        this.parent = parent;
        parentSize = parent.size();
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof PolylineSection)
        {
            var that = (PolylineSection) object;
            return shape().equals(that.shape())
                    && fromIndex == that.fromIndex
                    && toIndex == that.toIndex;
        }
        return false;
    }

    public int fromIndex()
    {
        return fromIndex;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(shape(), fromIndex, toIndex);
    }

    public Polyline parent()
    {
        assert (parent.size() == parentSize);
        return parent;
    }

    public Polyline shape()
    {
        return parent().shape(fromIndex, toIndex);
    }

    public int toIndex()
    {
        return toIndex;
    }

    @Override
    public String toString()
    {
        return "[PolylineSection parent = " + parent + ", fromIndex = " + fromIndex + ", toIndex = "
                + toIndex + ", shape = " + shape() + "]";
    }
}
