#!/usr/bin/env bash
set -euxo pipefail

docker image prune -f
docker system prune -f
docker compose up -d
