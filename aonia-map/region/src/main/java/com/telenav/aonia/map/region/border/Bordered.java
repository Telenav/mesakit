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

import com.telenav.aonia.map.geography.shape.polyline.Polygon;
import com.telenav.aonia.map.region.RegionIdentity;
import com.telenav.aonia.map.region.project.lexakai.diagrams.DiagramBorder;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.util.Collection;

@UmlClassDiagram(diagram = DiagramBorder.class)
public interface Bordered
{
    @UmlRelation(label = "bounded by", referentCardinality = "1+")
    Collection<Polygon> borders();

    RegionIdentity identity();
}
