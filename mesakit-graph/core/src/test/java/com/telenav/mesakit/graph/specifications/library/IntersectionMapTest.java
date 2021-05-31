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

package com.telenav.mesakit.graph.specifications.library;

import com.telenav.kivakit.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.kivakit.kernel.language.values.Count;
import com.telenav.kivakit.kernel.testing.RandomValueFactory;
import com.telenav.mesakit.graph.project.GraphCoreUnitTest;
import com.telenav.mesakit.graph.specifications.library.pbf.IntersectionMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntersectionMapTest extends GraphCoreUnitTest
{
    @Test
    public void test()
    {
        test(Arrays.asList(17, 10_001, 17, 5, 1, 17));
        test(Arrays.asList(1, 2, 2, 3, 2));
        test(randomIdentifiers(500, 20));
    }

    @SuppressWarnings("SameParameterValue")
    private List<Integer> randomIdentifiers(final int maximum, final int minimumIntersections)
    {
        // Create random value factory
        final RandomValueFactory random = randomValueFactory();

        final var identifiers = new ArrayList<Integer>();
        final Map<Integer, Integer> references = new HashMap<>();

        var intersections = 0;
        while (intersections < minimumIntersections)
        {
            final var identifier = random.newInt(1, maximum);
            identifiers.add(identifier);
            var count = references.get(identifier);
            if (count == null)
            {
                count = 1;
            }
            else
            {
                count++;
                if (count > 2)
                {
                    intersections++;
                }
            }
            references.put(identifier, count);
        }
        return identifiers;
    }

    @SuppressWarnings({ "SameParameterValue", "UnusedReturnValue" })
    private IntersectionMap test(final List<Integer> identifiers)
    {
        // Create map
        final var map = new IntersectionMap("test");

        // Map from identifier to count
        final Map<NodeIdentifier, Count> expected = new HashMap<>();

        // Loop through identifiers,
        for (final long identifier : identifiers)
        {
            // and reference the identifier as an identifier
            final var referenceCount = map.addReference(identifier);
            ensure(referenceCount > 0);

            // Store our expected count
            final var count = expected.computeIfAbsent(new PbfNodeIdentifier(identifier), k -> Count._0);
            expected.put(new PbfNodeIdentifier(identifier), count.incremented());
        }

        map.doneAdding();

        // For each identifier
        for (final NodeIdentifier identifier : expected.keySet())
        {
            // check that the count we tracked in the test matches the intersection method result
            final var expectedIntersection = expected.get(identifier).isGreaterThan(Count._2);
            final var intersection = map.isIntersection(identifier.asLong());
            ensureEqual(expectedIntersection, intersection);
        }

        return map;
    }
}
