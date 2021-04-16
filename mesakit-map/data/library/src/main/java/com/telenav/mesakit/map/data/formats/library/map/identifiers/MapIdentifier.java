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

import com.telenav.mesakit.map.data.formats.library.project.lexakai.diagrams.DiagramDataMapIdentifier;
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
