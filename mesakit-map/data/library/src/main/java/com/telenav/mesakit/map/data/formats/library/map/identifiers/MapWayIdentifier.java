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
import com.telenav.mesakit.map.data.formats.library.lexakai.DiagramDataMapIdentifier;

/**
 * OSM way identifier
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramDataMapIdentifier.class)
public abstract class MapWayIdentifier extends MapIdentifier
{
    public static final MapWayIdentifier NULL = new MapWayIdentifier(0)
    {
        @Override
        protected MapIdentifier newIdentifier(long asLong)
        {
            return null;
        }
    };

    /**
     * Construct from identifier
     */
    protected MapWayIdentifier(long identifier)
    {
        super(identifier);
    }

    public MapWayIdentifier backward()
    {
        return forward().reversed();
    }

    public MapWayIdentifier forward()
    {
        return (MapWayIdentifier) newIdentifier(Math.abs(asLong()));
    }

    public boolean isForward()
    {
        return !isReverse();
    }

    public boolean isReverse()
    {
        return asLong() < 0;
    }

    public MapWayIdentifier reversed()
    {
        return (MapWayIdentifier) newIdentifier(-1 * asLong());
    }

    @Override
    public Type type()
    {
        return Type.WAY;
    }
}
