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

package com.telenav.aonia.map.region.border;

import com.telenav.aonia.map.geography.indexing.rtree.InteriorNode;
import com.telenav.aonia.map.geography.indexing.rtree.Leaf;
import com.telenav.aonia.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.aonia.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.aonia.map.region.Region;
import com.telenav.aonia.map.region.project.lexakai.diagrams.DiagramBorder;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

@UmlClassDiagram(diagram = DiagramBorder.class)
@UmlRelation(label = "indexes", referent = Border.class, referentCardinality = "1+")
public class BorderSpatialIndex<T extends Region<T>> extends RTreeSpatialIndex<Border<T>>
{
    public BorderSpatialIndex(final String objectName, final RTreeSettings settings)
    {
        super(objectName, settings);
    }

    @Override
    public void add(final Border<T> element)
    {
        unsupported("BorderSpatialIndex only supports bulk loading of elements");
    }

    @Override
    public Leaf<Border<T>> newLeaf(final InteriorNode<Border<T>> parent)
    {
        return new BorderLeaf<>(this, parent);
    }
}
