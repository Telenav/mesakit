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

package com.telenav.mesakit.graph.world;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.resource.packages.Package;
import com.telenav.kivakit.settings.Deployment;
import com.telenav.kivakit.settings.DeploymentSet;
import com.telenav.kivakit.settings.stores.ResourceFolderSettingsStore;
import com.telenav.mesakit.core.MesaKit;

import static com.telenav.kivakit.core.project.Project.resolveProject;

/**
 * {@link WorldGraphDeployments} is a {@link DeploymentSet} that includes deployment configurations for a few built-in
 * world graph deployments (local, osmteam and navteam).
 *
 * @author jonathanl (shibo)
 * @see Deployment
 * @see DeploymentSet
 */
public class WorldGraphDeployments extends DeploymentSet
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * Returns a deployment of the world graph with a local repository in ~/.mesakit
     */
    public static Deployment localDeployment()
    {
        var deployment = new Deployment(LOGGER,"local", "developer laptop");
        deployment.indexAll(new ResourceFolderSettingsStore( LOGGER, Package.parsePackage(LOGGER, WorldGraph.class, "configuration/local")));
        return deployment;
    }

    /**
     * A set of built-in deployments
     */
    public WorldGraphDeployments(Listener listener)
    {
        listener.listenTo(this);
        addDeploymentsIn(mesakitWorldGraphDeploymentsFolder());
        add(localDeployment());
    }

    private Folder mesakitWorldGraphDeploymentsFolder()
    {
        return resolveProject(MesaKit.class).mesakitCacheFolder().folder("world-graph/deployments");
    }
}
