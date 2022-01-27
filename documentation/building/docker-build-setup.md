# MesaKit - Docker Build Setup   <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"/>

Docker makes it quick and easy to build MesaKit in any environment, without software setup hassles.

### Building MesaKit in Docker

Building MesaKit in Docker is a snap:

1. [Install docker](https://docs.docker.com/get-docker/)


2. If you have *NOT* [set up a local build](build-setup.md) already, choose a workspace:

       export MESAKIT_WORKSPACE=~/workspaces/mesakit
       export KIVAKIT_WORKSPACE=$MESAKIT_WORKSPACE

   and check out a fresh set of KivaKit and MesaKit repositories:

       mkdir -p $MESAKIT_WORKSPACE 
       cd $MESAKIT_WORKSPACE
       git clone --branch develop https://github.com/Telenav/kivakit.git
       bash $KIVAKIT_WORKSPACE/setup/setup-repositories.sh
       git clone --branch develop https://github.com/Telenav/mesakit.git
       bash $MESAKIT_WORKSPACE/setup/setup-repositories.sh


3. Next, launch the MesaKit build environment. If you have a local build set up, you can use the *mesakit-docker-run.sh* script. If you don't have a local build, set this variable to an image tag from [Docker Hub](https://hub.docker.com/repository/docker/jonathanlocke/mesakit):

       export MESAKIT_BUILD_IMAGE=0.9.9-snapshot

   and launch the build environment like this:

       docker run \
           --volume "$MESAKIT_WORKSPACE:/host/workspace" \
           --volume "$HOME/.m2:/host/.m2" \
           --volume "$HOME/.kivakit:/host/.kivakit" \
           --volume "$HOME/.mesakit:/host/.mesakit" \
           --interactive --tty "jonathanlocke/kivakit:$KIVAKIT_BUILD_IMAGE" \
           /bin/bash

   > The volume mounts here make the host workspace ($MESAKIT_WORKSPACE) and cache
   > folders ($HOME/.m2, $HOME/.kivakit) visible in Docker under /host. This makes it
   > possible for Docker to build your host workspace, which is useful when
   > working with an IDE.


4. The source code now can be built with:

       mesakit-build.sh

   and updated as desired with git.


5. Use the scripts in the table below to build MesaKit on your host or in Docker.


6. To switch to your host workspace (to work with an IDE):

       mesakit-docker-workspace.sh host

   Host locations:

    * MESAKIT_WORKSPACE => /host/workspace
    * /root/.m2 => /host/.m2
    * /root/.kivakit => /host/.kivakit
    * /root/.mesakit => /host/.mesakit


7. To switch your workspace back to Docker:

       kivakit-docker-workspace.sh docker

   Docker locations:

    * MESAKIT_WORKSPACE => /root/workspace
    * /root/.m2 => /root/developer/.m2
    * /root/.kivakit => /root/developer/.kivakit
    * /root/.mesakit => /root/developer/.mesakit

### MesaKit Build Scripts

| MesaKit Script                                 | Purpose                                   |
|------------------------------------------------|-------------------------------------------|
| mesakit-\[tab\]                                | see available mesakit shell scripts       |
| mesakit-version.sh                             | show mesakit version                      |
| mesakit-build.sh                               | build mesakit                             |
| mesakit-git-pull.sh                            | pull changes **                           |
| mesakit-git-checkout.sh \[branch\]             | check out the given branch **             |
| mesakit-docker-workspace.sh \[host or docker\] | switch between host and docker workspaces |
| mesakit-feature-start.sh \[branch\]            | start a feature branch **                 |
| mesakit-feature-finish.sh \[branch\]           | finish a feature branch **                |

** executes the command in each mesakit repository

