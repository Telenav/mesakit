////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.data.formats.library.map.identifiers;

import com.telenav.aonia.map.data.formats.library.project.lexakai.diagrams.DiagramDataMapIdentifier;
import com.telenav.lexakai.annotations.UmlClassDiagram;

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
        protected MapIdentifier newIdentifier(final long asLong)
        {
            return null;
        }
    };

    /**
     * Construct from identifier
     */
    protected MapWayIdentifier(final long identifier)
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
