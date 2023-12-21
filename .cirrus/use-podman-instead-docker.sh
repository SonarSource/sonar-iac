#!/bin/bash

function docker {
  podman "$@"
}

export -f docker
