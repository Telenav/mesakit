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

import com.telenav.mesakit.map.geography.shape.polyline.Polygon;

import java.util.Collection;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * Finds a collection of {@link Geohash}es that cover a given polygon.
 * <p>
 * Geohashes are compacted using a tolerance which determines how many sub-hashes must exist for the sub-hashes to be
 * compacted into a single {@link Geohash}. A tolerance can be set for interior compaction ( {@link
 * Builder#interiorCompactingTolerance(int)}) and a separate, finer tolerance can be set for fine-grained compaction
 * around the border of the polygon ( {@link Builder#borderCompactingTolerance(int)}).
 * <p>
 * The maximum depth of geohashing is the resolution. See {@link Builder#resolution(int)} for allowed values.
 * <p>
 * {@link Geohash}es may cross the polygon border if {@link Builder#allowBorderCrossings(boolean)} is true.
 *
 * @author Mihai Chintoanu
 * @author jonathanl (shibo)
 */
public class Geohasher
{
    public static class Builder
    {
        private final GeohashAlphabet alphabet;

        private int resolution = 5;

        private boolean allowBorderCrossings = true;

        private int interiorCompactingTolerance;

        private int borderCompactingTolerance;

        public Builder()
        {
            this(GeohashAlphabet.DEFAULT);
        }

        public Builder(final GeohashAlphabet alphabet)
        {
            this.alphabet = alphabet;
            if (resolution > alphabet.maximumTextLength())
            {
                resolution = alphabet.maximumTextLength() / 3;
            }
        }

        public Builder allowBorderCrossings(final boolean allowBorderCrossings)
        {
            this.allowBorderCrossings = allowBorderCrossings;
            return this;
        }

        public Builder borderCompactingTolerance(final int borderCompactingTolerance)
        {
            if (borderCompactingTolerance < 0 || borderCompactingTolerance >= alphabet.numberOfCharacters())
            {
                fail("The border compacting tolerance must be between 0 and " + (alphabet.numberOfCharacters() - 1));
            }
            this.borderCompactingTolerance = borderCompactingTolerance;
            return this;
        }

        public Geohasher build()
        {
            return new Geohasher(this);
        }

        public Builder interiorCompactingTolerance(final int interiorCompactingTolerance)
        {
            if (interiorCompactingTolerance < 0 || interiorCompactingTolerance >= alphabet.numberOfCharacters())
            {
                fail("The interior compacting tolerance must be between 0 and " + (alphabet.numberOfCharacters() - 1));
            }
            this.interiorCompactingTolerance = interiorCompactingTolerance;
            return this;
        }

        public Builder resolution(final int resolution)
        {
            if (resolution < 0 || resolution > alphabet.maximumTextLength())
            {
                fail("The resolution must be between 0 and " + alphabet.maximumTextLength());
            }
            this.resolution = resolution;
            return this;
        }
    }

    private final GeohashAlphabet alphabet;

    private final int resolution;

    private final int interiorCompactingTolerance;

    private final int borderCompactingTolerance;

    private final boolean allowBorderCrossings;

    private Geohasher(final Builder builder)
    {
        alphabet = builder.alphabet;
        resolution = builder.resolution;
        allowBorderCrossings = builder.allowBorderCrossings;
        interiorCompactingTolerance = builder.interiorCompactingTolerance;
        borderCompactingTolerance = builder.borderCompactingTolerance;
    }

    public Collection<Geohash> geohashes(final Polygon polygon)
    {
        final var organizer = new GeohashOrganizer(interiorCompactingTolerance,
                borderCompactingTolerance);
        geohashes(polygon, Geohash.world(alphabet), organizer);
        return organizer.all();
    }

    private void geohashes(final Polygon polygon, final Geohash geohash, final GeohashOrganizer organizer)
    {
        if (polygon.contains(geohash.bounds()))
        {
            organizer.add(geohash);
        }
        else if (geohash.bounds().contains(polygon.bounds()) || polygon.intersects(geohash.bounds()))
        {
            if (geohash.depth() < resolution)
            {
                for (final var child : geohash.children())
                {
                    geohashes(polygon, child, organizer);
                }
            }
            else if (geohash.depth() == resolution)
            {
                if (allowBorderCrossings)
                {
                    organizer.add(geohash);
                }
                // else ignore it
            }
            else
            {
                fail("Desired resolution exceeded");
            }
        }
        // else the geohash is exterior to the polygon, so ignore it altogether
    }
}
