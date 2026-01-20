#!/usr/bin/env bash

set -euxo pipefail

pushd messaging-utilities
./build.sh
popd

# Build the services
pushd user-service
./build.sh
popd

pushd payment-service
./build.sh
popd

pushd token-service
./build.sh
popd

pushd facade
./build.sh
popd
