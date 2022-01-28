#!/bin/bash

source library-functions.sh

help="[host|docker]"

WORKSPACE=$1

require_variable WORKSPACE "$help"

rm "$HOME"/.m2
rm "$HOME"/.kivakit
rm "$HOME"/.mesakit

if [ "$WORKSPACE" = "host" ]; then

    WORKSPACE="/host/workspace"

    ln -s /host/.m2 "$HOME"/.m2
    ln -s /host/.kivakit "$HOME"/.kivakit
    ln -s /host/.mesakit "$HOME"/.mesakit

elif [ "$WORKSPACE" = "docker" ]; then

    WORKSPACE="/root/workspace"

    ln -s "$DEVELOPER"/.m2 "$HOME"/.m2
    ln -s "$DEVELOPER"/.kivakit "$HOME"/.kivakit
    ln -s "$DEVELOPER"/.mesakit "$HOME"/.mesakit

else

    usage "$help"

fi

export KIVAKIT_WORKSPACE="$WORKSPACE"
export MESAKIT_WORKSPACE="$WORKSPACE"

source ~/.profile
