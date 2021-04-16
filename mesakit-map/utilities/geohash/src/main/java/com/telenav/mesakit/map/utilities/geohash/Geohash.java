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

package com.telenav.mesakit.map.utilities.geohash;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.kivakit.core.kernel.language.objects.Objects;

import java.util.Collection;
import java.util.HashSet;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * @author Mihai Chintoanu
 * @author jonathanl (shibo)
 * @see "http://en.wikipedia.org/wiki/Geohash"
 */
public class Geohash
{
    /**
     * When checking if a {@code geohash} is a world geohash, don't use {@code geohash.equals(Geohash.world())}; use
     * {@code geohash.isWorld()} instead. This is because {@code Geohash.world()} is built with a code that uses the
     * default alphabet, and if your {@code geohash} has a code that uses a different alphabet, {@code
     * geohash.equals(Geohash.world())} will return {@code false}.
     *
     * @return an imaginary geohash which encompasses the entire map, and uses the default alphabet
     */
    public static Geohash world()
    {
        return new Geohash(Code.root());
    }

    /**
     * @param alphabet the alphabet of the desired world geohash
     * @return an imaginary geohash which encompasses the entire map
     */
    public static Geohash world(final GeohashAlphabet alphabet)
    {
        return new Geohash(Code.root(alphabet));
    }

    private Rectangle bounds;

    private final Code code;

    public Geohash(final Code code)
    {
        this.code = ensureNotNull(code);
    }

    public Geohash(final GeohashAlphabet alphabet, final Location location, final int resolution)
    {
        this(new Codec(alphabet).encode(location, resolution));
    }

    /**
     * Builds a new geohash for the given location and resolution, using the default geohash alphabet.
     *
     * @param location a geographic location
     * @param resolution the desired resolution for the geohash
     */
    public Geohash(final Location location, final int resolution)
    {
        this(GeohashAlphabet.DEFAULT, location, resolution);
    }

    public Rectangle bounds()
    {
        if (bounds == null)
        {
            bounds = new Codec(alphabet()).decode(code);
        }
        return bounds;
    }

    /**
     * @return this geohash's children, or an empty collection if this geohash is at the maximum allowed depth
     */
    public Collection<Geohash> children()
    {
        final Collection<Geohash> children = new HashSet<>();
        if (depth() < maximumDepth())
        {
            for (final var child : code.children())
            {
                children.add(new Geohash(child));
            }
        }
        return children;
    }

    public Code code()
    {
        return code;
    }

    /**
     * Returns the "depth" of this geohash. The {@link Geohash#world()} geohash is by convention at depth 0. All its
     * children are at depth 1, and so on. Depending on your precision and performance requirements, you should use
     * deeper or shallower geohashes. A deep geohash has better precision than a shallow one, but you need more of them
     * to cover the same area. The precision depends on the used {@code GeohashAlphabet}. See your preferred {@code
     * GeohashAlphabet} implementation's documentation for details.
     *
     * @return A positive integer representing the depth of this geohash
     */
    public int depth()
    {
        return code().length();
    }

    @Override
    public boolean equals(final Object that)
    {
        if (that instanceof Geohash)
        {
            return Objects.equal(code, ((Geohash) that).code);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    public boolean isWorld()
    {
        return code().isRoot();
    }

    public int maximumDepth()
    {
        return code.alphabet().maximumTextLength();
    }

    public int numberOfChildren()
    {
        return alphabet().numberOfCharacters();
    }

    /**
     * Returns this geohash's parent, or null if this geohash is {@link Geohash#world()}.
     *
     * @return the parent of this geohash
     */
    public Geohash parent()
    {
        return isWorld() ? null : new Geohash(code.parent());
    }

    @Override
    public String toString()
    {
        return code + " (" + bounds() + ')';
    }

    private GeohashAlphabet alphabet()
    {
        return code.alphabet();
    }
}
