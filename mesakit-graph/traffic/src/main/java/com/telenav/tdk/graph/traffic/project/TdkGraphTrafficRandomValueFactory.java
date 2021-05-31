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

package com.telenav.kivakit.graph.traffic.project;

import com.telenav.kivakit.configuration.Lookup;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSection;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionDatabase;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier;
import com.telenav.kivakit.map.geography.project.KivaKitMapGeographyRandomValueFactory;

import static com.telenav.kivakit.kernel.validation.Validate.unsupported;

public class KivaKitGraphTrafficRandomValueFactory extends KivaKitMapGeographyRandomValueFactory
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public RoadSection newRoadSection()
    {
        while (true)
        {
            final var section = getRoadSectionDatabase().roadSectionForIdentifier(newRoadSectionIdentifier());
            if (section.start().equals(section.end()))
            {
                LOGGER.warning("Bad roadsection with zero length: $", section.identifier());
            }
            else
            {
                return section;
            }
        }
    }

    public RoadSectionIdentifier newRoadSectionIdentifier()
    {
        final var identifiers = getRoadSectionDatabase().identifiers();
        return identifiers.get(newIndex(identifiers.size()));
    }

    private RoadSectionDatabase getRoadSectionDatabase()
    {
        final var database = Lookup.global().locate(RoadSectionDatabase.class);
        if (database == null)
        {
            unsupported("Road section database is not available");
        }
        return database;
    }
}
