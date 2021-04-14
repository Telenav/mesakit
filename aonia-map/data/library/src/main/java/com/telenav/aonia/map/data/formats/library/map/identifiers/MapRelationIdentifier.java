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
 * OSM relation identifier
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramDataMapIdentifier.class)
public abstract class MapRelationIdentifier extends MapIdentifier
{
    public static final MapRelationIdentifier NULL = new MapRelationIdentifier(0)
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
    protected MapRelationIdentifier(final long identifier)
    {
        super(identifier);
    }

    @Override
    public Type type()
    {
        return Type.RELATION;
    }
}
