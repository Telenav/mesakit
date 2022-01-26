# MesaKit - Docker Build Setup   <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"/>

Docker makes it quick and easy to build MesaKit in any environment, without setup hassles.

### Building MesaKit in Docker

Building MesaKit (and KivaKit) in Docker is a snap:

1. [Install docker](https://docs.docker.com/get-docker/)
2. In a shell window on your host:

       docker -it jonathanlocke/mesakit:[version]

   The [MesaKit Docker build environment image]( https://hub.docker.com/repository/docker/jonathanlocke/mesakit) of the specified version will launch with the *develop* branch checked out. The source code can be built with:

       mesakit-build.sh

   and updated as desired with git. The scripts *mesakit-git-pull.sh* and *mesakit-git-checkout.sh* conveniently operate across all mesakit repositories.

3. Use the scripts in the table below to build MesaKit on your host or in the Docker container. To switch your build workspace from the container (/root/workspace) to your host (/host/workspace), execute the command:

       mesakit-docker-workspace.sh host

   to switch back to the container workspace:

       mesakit-docker-workspace.sh container

   > **NOTE**
   >
   > To switch to the host workspace, it must be mounted as a volume in the container like this:
   >
   >     docker -v "$MESAKIT_WORKSPACE:/host/workspace" [...]
   >
   > For convenience, the script *mesakit-docker-run.sh* launches docker with this volume mounted.

### MesaKit Scripts

| MesaKit Script                                    | Purpose                                      |
|---------------------------------------------------|----------------------------------------------|
| mesakit-\[tab\]                                   | see available mesakit shell scripts          |
| mesakit-version.sh                                | show mesakit version                         |
| mesakit-build.sh                                  | build mesakit                                |
| mesakit-git-pull.sh                               | pull changes **                              |
| mesakit-git-checkout.sh \[branch\]                | check out the given branch **                |
| mesakit-docker-workspace.sh \[host or container\] | switch between host and container workspaces |
| mesakit-feature-start.sh \[branch\]               | start a feature branch **                    |
| mesakit-feature-finish.sh \[branch\]              | finish a feature branch **                   |

** executes the command in each mesakit repository
