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

package com.telenav.mesakit.map.geography.shape.rectangle;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramRectangle;

@UmlClassDiagram(diagram = DiagramRectangle.class)
public class Offset implements Dimensioned
{
    private final Width width;

    private final Height height;

    public Offset(final Width width, final Height height)
    {
        this.height = height;
        this.width = width;
    }

    @Override
    public Height height()
    {
        return height;
    }

    @Override
    public String toString()
    {
        return "[Offset width = " + width.asDegrees() + ", height = " + height.asDegrees() + "]";
    }

    @Override
    public Width width()
    {
        return width;
    }
}
