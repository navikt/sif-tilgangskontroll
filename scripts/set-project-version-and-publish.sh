#!/bin/bash

newProjectVersion=$(./scripts/get-version.sh)
echo $newProjectVersion
./gradlew -Pversion="${newProjectVersion}" publish
