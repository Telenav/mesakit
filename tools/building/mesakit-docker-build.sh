#!/bin/bash

cd "$MESAKIT_HOME"/tools/building/docker || exit

if [ "$1" = "" ]; then
    IMAGE_VERSION=$(echo "$MESAKIT_VERSION" | tr '[:upper:]' '[:lower:]')
else
    IMAGE_VERSION=$(echo "$1" | tr '[:upper:]' '[:lower:]')
fi

docker run \
    --volume "$TELENAV_WORKSPACE:/host/workspace" \
    --volume "$HOME/.m2:/host/.m2" \
    --volume "$HOME/.kivakit:/host/.kivakit" \
    --volume "$HOME/.mesakit:/host/.mesakit" \
    --interactive --tty "jonathanlocke/mesakit:$IMAGE_VERSION" \
    /bin/bash
