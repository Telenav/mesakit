////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.data.formats.library.map.identifiers;

import com.telenav.aonia.map.data.formats.library.project.lexakai.diagrams.DiagramDataMapIdentifier;
import com.telenav.kivakit.core.kernel.language.values.identifier.Identifier;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all map identifiers in all data specifications.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramDataMapIdentifier.class)
public abstract class MapIdentifier extends Identifier
{
    /**
     * All map identifiers are 64 bits, although the actual storage size may vary.
     */
    public static final int SIZE_IN_BITS = 64;

    public enum Type
    {
        INVALID(0),
        NODE(1),
        WAY(2),
        RELATION(3);

        private static final Map<Integer, Type> forOrdinal = new HashMap<>();

        static
        {
            for (final var type : values())
            {
                forOrdinal.put(type.identifier(), type);
            }
        }

        public static Type forOrdinal(final int ordinal)
        {
            return forOrdinal.get(ordinal);
        }

        private final int identifier;

        Type(final int identifier)
        {
            this.identifier = identifier;
        }

        public int identifier()
        {
            return identifier;
        }
    }

    /**
     * Construct from primitive identifier
     */
    protected MapIdentifier(final long identifier)
    {
        super(identifier);
    }

    /**
     * @return The next identifier after this one
     */
    public MapIdentifier next()
    {
        return newIdentifier(asLong() + 1);
    }

    /**
     * @return The type of map identifier
     */
    public abstract Type type();

    /**
     * @param identifier The primitive identifier
     * @return Map identifier
     */
    protected abstract MapIdentifier newIdentifier(long identifier);
}
