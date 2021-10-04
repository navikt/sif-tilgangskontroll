#!/bin/bash
majorVersion="1"
gitShortHash=$(git rev-parse --short HEAD)
gitBranch=$(git branch --show-current)
newProjectVersion="${majorVersion}-${gitShortHash}"

if [ ${gitBranch} == "master" ]; then
  echo "${newProjectVersion}"
else
  echo "${newProjectVersion}-RC"
fi
