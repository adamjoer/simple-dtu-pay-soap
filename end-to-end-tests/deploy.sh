#!/bin/bash
set -e
docker image prune -f
docker system prune -f
docker-compose up -d

