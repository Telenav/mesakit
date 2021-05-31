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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.data.formats.library.DataFormat;
import com.telenav.mesakit.graph.metadata.DataBuild;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.metadata.DataVersion;
import com.telenav.mesakit.graph.project.KivaKitGraphCoreUnitTest;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class MetadataTest extends KivaKitGraphCoreUnitTest
{
    @Test
    public void testParse()
    {
        ensureEqual(Metadata.parse("OSM-OSM-PBF-Test"), new Metadata()
                .withDataSpecification(OsmDataSpecification.get())
                .withDataSupplier(DataSupplier.OSM)
                .withDataFormat(DataFormat.PBF)
                .withName("Test"));

        ensureEqual(Metadata.parse("OSM-OSM-PBF-Test-2020Q2"), new Metadata()
                .withDataSpecification(OsmDataSpecification.get())
                .withDataSupplier(DataSupplier.OSM)
                .withDataFormat(DataFormat.PBF)
                .withName("Test")
                .withDataVersion(DataVersion.parse("2020Q2")));

        ensureEqual(Metadata.parse("OSM-OSM-PBF-Test-2020Q2-2020.04.01_04.01PM_PT"), new Metadata()
                .withDataSpecification(OsmDataSpecification.get())
                .withDataSupplier(DataSupplier.OSM)
                .withDataFormat(DataFormat.PBF)
                .withName("Test")
                .withDataVersion(DataVersion.parse("2020Q2"))
                .withDataBuild(DataBuild.parse("2020.04.01_04.01PM_PT")));

        ensureEqual(Metadata.parse("OSM-OSM-PBF-Bellevue_Washington-47.5840818_-122.2169381_47.6419581_-122.1590618"), new Metadata()
                .withDataSpecification(OsmDataSpecification.get())
                .withDataSupplier(DataSupplier.OSM)
                .withDataFormat(DataFormat.PBF)
                .withName("Bellevue_Washington")
                .withDataBounds(Rectangle.parse("-47.5840818,-122.2169381:47.6419581,-122.1590618")));

        ensureEqual(Metadata.parse("OSM-OSM-PBF-Bellevue_Washington-47.5840818_-122.2169381_47.6419581_-122.1590618-2020Q2-2020.04.01_04.01PM_PT"), new Metadata()
                .withDataSpecification(OsmDataSpecification.get())
                .withDataSupplier(DataSupplier.OSM)
                .withDataFormat(DataFormat.PBF)
                .withName("Bellevue_Washington")
                .withDataBounds(Rectangle.parse("-47.5840818,-122.2169381:47.6419581,-122.1590618"))
                .withDataVersion(DataVersion.parse("2020Q2"))
                .withDataBuild(DataBuild.parse("2020.04.01_04.01PM_PT")));
    }
}
