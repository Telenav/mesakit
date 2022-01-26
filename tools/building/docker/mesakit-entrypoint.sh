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
echo "┋  mesakit-docker-workspace.sh [host|docker]     switch between host and docker workspaces"
echo "┋  mesakit-feature-start.sh [branch]             start a feature branch *"
echo "┋  mesakit-feature-finish.sh [branch]            finish a feature branch *"
echo "┋"
echo "┋  kivakit-[tab]                                 see kivakit shell scripts"
echo "┋  kivakit-version.sh                            show kivakit version"
echo "┋  kivakit-build.sh                              build kivakit"
echo "┋  kivakit-git-pull.sh                           pull changes **"
echo "┋  kivakit-git-checkout.sh [branch]              check out the given branch **"
echo "┋  kivakit-docker-workspace.sh [host|docker]     switch between host and docker workspaces"
echo "┋  kivakit-feature-start.sh [branch]             start a feature branch **"
echo "┋  kivakit-feature-finish.sh [branch]            finish a feature branch **"
echo "┋"
echo "┋ * executes the command in each mesakit repository"
echo "┋ ** executes the command in each kivakit repository"
echo "┋"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo " "

$SHELL