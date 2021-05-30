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


package com.telenav.tdk.graph.library.osm.change;

import com.telenav.tdk.data.formats.library.map.identifiers.WayIdentifier;

import static com.telenav.tdk.core.kernel.validation.Validate.ensureNotNull;

/**
 * Represents a way to remove
 *
 * @author jonathanl (shibo)
 */
public class RemovedWay
{
    // The identifier of this new way
    private final WayIdentifier identifier;

    public RemovedWay(final WayIdentifier identifier)
    {
        this.identifier = ensureNotNull(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RemovedWay)
        {
            final var that = (RemovedWay) object;
            return identifier.equals(that.identifier);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return identifier.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "  <way id=\"" + identifier + "\" action=\"delete\" timestamp=\"" + new Timestamp()
                + "\" uid=\"2100001\" user=\"scout_osm\"/>";
    }
}
