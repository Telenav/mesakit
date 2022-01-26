#!/bin/bash

source /root/.profile

echo " "
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ MesaKit Docker Build Help  ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┋  "
echo "┋  Command                                       Description"
echo "┋  ------------------------------------------    ---------------------------"
echo "┋  mesakit-[tab]                                 see mesakit shell scripts"
echo "┋  mesakit-version.sh                            show mesakit version"
echo "┋  mesakit-build.sh                              build mesakit"
echo "┋  mesakit-git-pull.sh                           pull changes *"
echo "┋  mesakit-git-checkout.sh [branch]              check out the given branch *"
echo "┋  mesakit-docker-workspace.sh [host|container]  switch between host and container workspaces"
echo "┋  mesakit-feature-start.sh [branch]             start a feature branch *"
echo "┋  mesakit-feature-finish.sh [branch]            finish a feature branch *"
echo "┋"
echo "┋ * executes the command in each mesakit repository"
echo "┋"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo " "

/root/kivakit-entrypoint.sh
