# MesaKit - Docker Build Setup   <img src="https://telenav.github.io/telenav-assets/images/icons/box-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/box-32-2x.png 2x"/>

Docker makes it quick and easy to build MesaKit in any environment, without software setup hassles.

### Building MesaKit in Docker

Building MesaKit in Docker is a snap:

1. Install [Docker Desktop](https://docs.docker.com/get-docker/) and ensure that your home folder can be shared under Settings / Resources / File Sharing


2. If you have *NOT* [set up a local build](host-build-environment.md) already, choose a workspace:

       export MESAKIT_WORKSPACE=~/workspaces/mesakit
       export KIVAKIT_WORKSPACE=$MESAKIT_WORKSPACE

   and check out a fresh set of KivaKit and MesaKit repositories:

       mkdir -p $MESAKIT_WORKSPACE 
       cd $MESAKIT_WORKSPACE
       git clone --branch develop https://github.com/Telenav/kivakit.git
       bash $KIVAKIT_WORKSPACE/kivakit/setup/setup-repositories.sh
       git clone --branch develop https://github.com/Telenav/mesakit.git
       bash $MESAKIT_WORKSPACE/mesakit/setup/setup-repositories.sh


3. Next, launch the MesaKit build environment. If you have a local build set up, you can use the *mesakit-docker-build.sh* script. If you don't have a local build, set this variable to an image tag from [Docker Hub](https://hub.docker.com/repository/docker/jonathanlocke/mesakit):

       export MESAKIT_BUILD_IMAGE=0.9.9-snapshot

   and launch the build environment like this:

       docker run \
           --volume "$MESAKIT_WORKSPACE:/host/workspace" \
           --volume "$HOME/.m2:/host/.m2" \
           --volume "$HOME/.kivakit:/host/.kivakit" \
           --volume "$HOME/.mesakit:/host/.mesakit" \
           --interactive --tty "jonathanlocke/mesakit:$MESAKIT_BUILD_IMAGE" \
           /bin/bash

   > The volume mounts here make the host workspace ($MESAKIT_WORKSPACE) and cache
   > folders ($HOME/.m2, $HOME/.kivakit) visible in Docker under /host. This makes it
   > possible for Docker to build your host workspace, which is useful when
   > working with an IDE.


4. Use the scripts in the table below to build MesaKit.


5. To switch from the Docker workspace (default) to your host workspace:

       mesakit-docker-build-workspace.sh host

   Host locations:

    * MESAKIT_WORKSPACE => /host/workspace
    * /root/.m2 => /host/.m2
    * /root/.kivakit => /host/.kivakit
    * /root/.mesakit => /host/.mesakit


6. To switch your workspace back to Docker:

       mesakit-docker-build-workspace.sh docker

   Docker locations:

    * MESAKIT_WORKSPACE => /root/workspace
    * /root/.m2 => /root/developer/.m2
    * /root/.kivakit => /root/developer/.kivakit
    * /root/.mesakit => /root/developer/.mesakit

<img src="https://telenav.github.io/telenav-assets/images/icons/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

### MesaKit Build Scripts

| MesaKit Script                       | Purpose                                   |
|--------------------------------------|-------------------------------------------|
| mesakit-\[tab\]                      | see available mesakit shell scripts       |
| mesakit-version.sh                   | show mesakit version                      |
| mesakit-build.sh                     | build mesakit                             |
| mesakit-git-pull.sh                  | pull changes **                           |
| mesakit-git-checkout.sh \[branch\]   | check out the given branch **             |
| mesakit-docker-build-workspace.sh    | switch between host and docker workspaces |
| mesakit-feature-start.sh \[branch\]  | start a feature branch **                 |
| mesakit-feature-finish.sh \[branch\] | finish a feature branch **                |

** executes the command in each mesakit repository
