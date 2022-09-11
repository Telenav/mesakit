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

package com.telenav.mesakit.map.data.formats.library.map.identifiers;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.library.internal.lexakai.DiagramDataMapIdentifier;

/**
 * OSM node identifier
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramDataMapIdentifier.class)
public abstract class MapNodeIdentifier extends MapIdentifier
{
    public static class Comparator implements java.util.Comparator<MapNodeIdentifier>
    {
        @Override
        public int compare(MapNodeIdentifier a, MapNodeIdentifier b)
        {
            return a.compareTo(b);
        }
    }

    /**
     * Construct from identifier
     */
    protected MapNodeIdentifier(long identifier)
    {
        super(identifier);
    }

    @Override
    public Type type()
    {
        return Type.NODE;
    }
}
