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

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramPolyline;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolygonBuilder extends PolylineBuilder
{
    @Override
    @UmlRelation(label = "builds")
    public Polygon build()
    {
        ensure(size() >= 3, "Incomplete polygon");
        ensure(!hasDuplicates(), "Contains duplicated locations");

        if (!end().equals(start()))
        {
            add(start());
        }
        if (isValid())
        {
            return new Polygon(locations());
        }
        return null;
    }

    @Override
    public boolean isValid()
    {
        return !hasDuplicates() && size() >= 3;
    }
}
