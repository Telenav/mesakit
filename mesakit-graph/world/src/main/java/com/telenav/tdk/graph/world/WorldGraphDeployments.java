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

package com.telenav.tdk.graph.world;

import com.telenav.tdk.core.configuration.*;
import com.telenav.tdk.core.filesystem.Folder;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.messaging.*;
import com.telenav.tdk.core.kernel.path.PackagePath;

/**
 * {@link WorldGraphDeployments} is a {@link DeploymentSet} that includes deployment configurations for a few built-in
 * world graph deployments (local, osmteam and navteam). A deployment from this set of configurations can be selected
 * with {@link #switchParser()} or {@link #switchParser(String)}. The PbfWorldGraphExtractorApplication provides a good
 * example of this.
 *
 * @author jonathanl (shibo)
 * @see Deployment
 * @see DeploymentSet
 */
public class WorldGraphDeployments extends DeploymentSet
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * @return A deployment of the world graph with a local repository at ~/tdk/graph/world-graph/repositories/local
     */
    public static Deployment localDeployment()
    {
        return LOGGER.listenTo(new Deployment("local", "developer laptop"))
                .loadAll(PackagePath.parse(WorldGraph.class, "configuration/local"));
    }

    /**
     * @return A deployment of the world graph for the navteam, with a local repository at
     * ~/tdk/graph/world-graph/repositories/local and a remote repository at hdfs://navteam/tdk/world-graph/repositories/navteam
     */
    public static Deployment navteamDeployment()
    {
        return LOGGER.listenTo(new Deployment("navteam", "nav team data"))
                .loadAll(PackagePath.parse(WorldGraph.class, "configuration/navteam"));
    }

    /**
     * @return A deployment of the world graph for the osmteam, with a local repository at
     * ~/tdk/graph/world-graph/repositories/local and a remote repository at hdfs://osmteam/tdk/world-graph/repositories/osmteam
     */
    public static Deployment osmteamDeployment()
    {
        return LOGGER.listenTo(new Deployment("osmteam", "osm team data"))
                .loadAll(PackagePath.parse(WorldGraph.class, "configuration/osmteam"));
    }

    /**
     * A set of 4 built-in deployments loaded from packages: local, osmteam and navteam. In addition, any deployments
     * found in the folder ~/tdk/graph/world-graph/deployments are included.
     */
    public WorldGraphDeployments(final Listener<Message> listener)
    {
        listener.listenTo(this);
        addDeployments(tdkWorldGraphDeploymentsFolder());
        add(localDeployment());
        add(osmteamDeployment());
        add(navteamDeployment());
    }

    private Folder tdkWorldGraphDeploymentsFolder()
    {
        return Folder.tdkConfigurationFolder().folder("world-graph/deployments");
    }
}
