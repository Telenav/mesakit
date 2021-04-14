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
        public int compare(final MapNodeIdentifier a, final MapNodeIdentifier b)
        {
            return a.compareTo(b);
        }
    }

    /**
     * Construct from identifier
     */
    protected MapNodeIdentifier(final long identifier)
    {
        super(identifier);
    }

    @Override
    public Type type()
    {
        return Type.NODE;
    }
}
