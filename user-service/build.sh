#!/usr/bin/env bash
set -euxo pipefail

mvn clean package

docker build -f Dockerfile -t user-service .
