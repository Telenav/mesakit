////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.identifiers;

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.kernel.data.conversion.BaseConverter;
import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitExcludeProperty;
import com.telenav.kivakit.kernel.language.values.identifier.Identifier;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;

/**
 * An identifier of {@link Edge}s in a {@link Graph} that can be used to retrieve the identified edge with {@link
 * Graph#edgeForIdentifier(EdgeIdentifier)}. Edge identifiers can be forward (positive sign) or reverse (negative sign).
 * This can be determined with {@link #isForward()} and {@link #isReverse()}.  The forward identifier can be retrieved
 * for any edge identifier with {@link #asForward()}. The edge identifier in the opposite direction can be retrieved
 * with {@link #reversed()}, which changes the sign from positive to negative or negative to positive. In certain
 * applications, it may be desirable to retrieve this identifier without the wrapper object. This can be achieved with
 * {@link Edge#identifierAsLong()}.
 * <p>
 * Edge identifiers have a numeric scheme that adds edge-specific information to an underlying {@link PbfWayIdentifier},
 * which is a kind of {@link MapIdentifier} in the original source data. The method {@link #asWayIdentifier()} can be
 * used to extract the {@link PbfWayIdentifier} from the edge identifier. {@link #asDirectionalWayIdentifier()} provides
 * the same identifier, but with a negative sign if the edge identifier is negative.
 *
 * @author jonathanl (shibo)
 * @see Graph
 * @see Graph#edgeForIdentifier(EdgeIdentifier)
 * @see Edge#identifier()
 * @see Edge#identifierAsLong()
 * @see PbfWayIdentifier
 */
public class EdgeIdentifier extends Identifier implements GraphElementIdentifier
{
    public static final int SIZE_IN_BITS = 64;

    public static final EdgeIdentifier INVALID = new EdgeIdentifier(-1L);

    /**
     * The low 6 decimal digits of each edge identifier are reserved for sequence numbers produced when ways are split
     * into edges by clean cutting and edge sectioning. This scheme preserves the way identifier in the digits above the
     * bottom 6 digits while allowing each edge to have a unique identifier.
     *
     * <pre>
     * IIIIIIIIII SSSSSS
     *
     * I = way identifier
     * S = 6 digit sequence number
     * </pre>
     */
    public static final long MAXIMUM_SEQUENCE_NUMBER = 999_999;

    public static final long SEQUENCE_NUMBER_SHIFT = 1_000_000;

    /**
     * The sequence number for an edge increases by this increment for each edge section within a given clean-cut
     * chunk.
     *
     * <pre>
     * IIIIIIIIII CC EEEE
     *
     * I = way identifier
     * C = chunk number
     * E = edge section number
     * </pre>
     */
    public static final long SEQUENCE_NUMBER_EDGE_SECTION_INCREMENT = 1;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * The sequence number is incremented by this value for each 'chunk' that is produced by clean cutting. Note that
     * this will only happen if a way wanders back and forth across the clean-cutting boundary, producing two or more
     * chunks. Individual edge sections for each chunk are in the lowest decimal digits labeled 'E'.
     *
     * <pre>
     * IIIIIIIIII CC EEEE
     *
     * I = way identifier
     * C = chunk number
     * E = edge section number
     * </pre>
     */
    private static final long MAXIMUM_CHUNK_NUMBER = 9999;

    private static final long CHUNK_NUMBER_INCREMENT = MAXIMUM_CHUNK_NUMBER + 1;

    public static SwitchParser.Builder<EdgeIdentifier> edgeIdentifierSwitchParser(final String name,
                                                                                  final String description)
    {
        return SwitchParser.builder(EdgeIdentifier.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER));
    }

    /**
     * @return The way identifier for the given element identifier. This is obtained by shifting the element identifier
     * six decimal places to the right, removing the sequence number that was added to the way identifier.
     */
    public static long identifierToWayIdentifier(final long elementIdentifier)
    {
        return Math.abs(elementIdentifier) / SEQUENCE_NUMBER_SHIFT;
    }

    /**
     * This class indicates the format of edge identifier
     *
     * @author songg
     * @author jonathanl (shibo)
     */
    public enum Type
    {
        // One long integer as the identifier
        LONG_IDENTIFIER,

        // A Skobbler edge identifier of the form
        // "<way-identifier>:<from-node-identifier>:<to-node-identifier>"
        EDGE_IDENTIFIER,

        // The edge identifier output will be dynamic decided based on graph
        CORRECTION_IDENTIFIER;

        public static Type forString(final String value)
        {
            if (value.contains(":"))
            {
                return EDGE_IDENTIFIER;
            }
            else
            {
                return LONG_IDENTIFIER;
            }
        }
    }

    public static class Converter extends Quantizable.Converter<EdgeIdentifier>
    {
        public Converter(final Listener listener)
        {
            super(listener, EdgeIdentifier::new);
        }
    }

    /***
     * Convert {@link EdgeIdentifier} to {@link PbfWayIdentifier}
     *
     * @author songg
     */
    public static class ToWayIdentifierConverter extends BaseConverter<EdgeIdentifier, PbfWayIdentifier>
    {
        public ToWayIdentifierConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected PbfWayIdentifier onConvert(final EdgeIdentifier value)
        {
            return value.asWayIdentifier();
        }
    }

    /**
     * Construct from identifier
     */
    public EdgeIdentifier(final long identifier)
    {
        super(identifier);
    }

    /**
     * @return A directional PBF way identifier for this edge identifier (by stripping off the low 4 digits)
     */
    public MapWayIdentifier asDirectionalWayIdentifier()
    {
        final var wayIdentifier = new PbfWayIdentifier(Math.abs(asLong()) / SEQUENCE_NUMBER_SHIFT);
        if (isReverse())
        {
            return wayIdentifier.reversed();
        }
        return wayIdentifier;
    }

    /**
     * @return This edge as a forward (positive) edge identifier (if it isn't already). If the edge is a reversed
     * identifier (negative value), the corresponding positive edge identifier is returned.
     */
    public EdgeIdentifier asForward()
    {
        if (isReverse())
        {
            return new EdgeIdentifier(-1 * asLong());
        }
        return this;
    }

    /**
     * @return An way identifier for this edge identifier
     */
    public PbfWayIdentifier asWayIdentifier()
    {
        final var wayIdentifier = identifierToWayIdentifier(asLong());
        assert wayIdentifier > 0 : "Cannot derive way identifier derived from identifier " + this;
        return new PbfWayIdentifier(wayIdentifier);
    }

    /**
     * Implementation of {@link GraphElementIdentifier#element(Graph)}.
     *
     * @return The graph element from the given graph for this identifier
     */
    @Override
    public GraphElement element(final Graph graph)
    {
        return graph.edgeForIdentifier(this);
    }

    /**
     * @return True if this edge identifier is for a forward edge
     */
    @KivaKitExcludeProperty
    public boolean isForward()
    {
        return !isReverse();
    }

    /**
     * @return True if this edge identifier is for a reversed edge (a negative identifier)
     */
    @KivaKitExcludeProperty
    public boolean isReverse()
    {
        return asLong() < 0;
    }

    /**
     * @return The next identifier after this one
     */
    public EdgeIdentifier next()
    {
        // Make sure that we're not out of edge sequence numbers
        assert sequenceNumber() != MAXIMUM_CHUNK_NUMBER : "Decoded number for edge " + this + " has overflowed";

        return new EdgeIdentifier(asLong() + SEQUENCE_NUMBER_EDGE_SECTION_INCREMENT);
    }

    /**
     * @return The next identifier after this one
     */
    public EdgeIdentifier nextChunk()
    {
        return new EdgeIdentifier(asLong() + CHUNK_NUMBER_INCREMENT);
    }

    /**
     * @return An edge identifier in the opposite direction. If this edge identifier is forward (positive identifier),
     * the reverse identifier will be returned (negative identifier). If the edge identifier is reverse (negative
     * identifier), the forward identifier will be returned (positive identifier).
     */
    public EdgeIdentifier reversed()
    {
        return new EdgeIdentifier(asLong() * -1);
    }

    /**
     * @return The sequence number of this edge identifier. For example, the sequence number of edge 1234 000 003 is 3.
     */
    public int sequenceNumber()
    {
        return (int) (asLong() % SEQUENCE_NUMBER_SHIFT);
    }

    /**
     * @return This identifier with room for sequence numbers. For example, identifier 1234 becomes 1234 000 000
     */
    public EdgeIdentifier sequenceNumbered()
    {
        return new EdgeIdentifier(asLong() * SEQUENCE_NUMBER_SHIFT);
    }

    public EdgeIdentifier unqualified()
    {
        return this;
    }

    /**
     * @return This number with the given sequence number. For example, identifier 12340003 with sequence number 7
     * becomes 12340007
     */
    public EdgeIdentifier withSequenceNumber(final int sequenceNumber)
    {
        assert sequenceNumber < MAXIMUM_SEQUENCE_NUMBER : "Invalid sequence number " + sequenceNumber;
        return new EdgeIdentifier((asLong() / SEQUENCE_NUMBER_SHIFT * SEQUENCE_NUMBER_SHIFT) + sequenceNumber);
    }
}
