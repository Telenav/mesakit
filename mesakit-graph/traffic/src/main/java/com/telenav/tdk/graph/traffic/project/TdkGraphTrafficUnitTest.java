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

package com.telenav.tdk.graph.traffic.project;

import com.telenav.tdk.core.kernel.operation.progress.ProgressReporter;
import com.telenav.tdk.graph.traffic.roadsection.RoadSectionDatabase;
import com.telenav.tdk.map.geography.project.TdkMapGeographyUnitTest;
import org.junit.BeforeClass;

public class TdkGraphTrafficUnitTest extends TdkMapGeographyUnitTest
{
    @BeforeClass
    public static void testSetup()
    {
        TdkGraphTraffic.get().install();
    }

    protected void loadBayAreaRoadSectionDatabase()
    {
        RoadSectionDatabase.load(this, ProgressReporter.NULL, "RoadSectionDatabase-TT-Bay-Area.csv.gz");
    }

    protected void loadBrazilRoadSectionDatabase()
    {
        RoadSectionDatabase.load(this, ProgressReporter.NULL, "RoadSectionDatabase-TT-BR.csv.gz");
    }

    protected void loadCaliforniaRoadSectionDatabase()
    {
        RoadSectionDatabase.load(this, ProgressReporter.NULL, "RoadSectionDatabase-OSM-US-California.csv.gz");
    }

    protected void loadEastCoastRoadSectionDatabase()
    {
        RoadSectionDatabase.load(this, ProgressReporter.NULL, "RoadSectionDatabase-TT-NA-East.csv.gz");
    }

    protected void loadWestCoastRoadSectionDatabase()
    {
        RoadSectionDatabase.load(this, ProgressReporter.NULL, "RoadSectionDatabase-TT-NA-West.csv.gz");
    }

    @Override
    protected TdkGraphTrafficRandomValueFactory randomValueFactory()
    {
        return newRandomValueFactory(TdkGraphTrafficRandomValueFactory::new);
    }
}
