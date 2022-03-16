#!/bin/bash

IMAGE_VERSION=$(echo "$MESAKIT_VERSION" | tr '[:upper:]' '[:lower:]')

docker push jonathanlocke/mesakit:$IMAGE_VERSION

