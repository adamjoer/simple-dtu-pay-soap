#!/usr/bin/env bash
set -euf -o pipefail

IMAGE_NAME="code-with-quarkus-jvm"
CONTAINER_NAME="code-with-quarkus-test"
SERVICE_DIR="service"
CLIENT_DIR="client"
DOCKERFILE="$SERVICE_DIR/src/main/docker/Dockerfile.jvm"
STARTUP_URL="http://localhost:8080/hello"
STARTUP_TIMEOUT=60
STARTUP_INTERVAL=2

cleanup() {
  echo "Cleaning up container ${CONTAINER_NAME}..."
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "1) Building service (skip tests)..."
mvn -f "${SERVICE_DIR}" clean package

echo "2) Building Docker image ${IMAGE_NAME}..."
docker build -f "${DOCKERFILE}" -t "${IMAGE_NAME}" "${SERVICE_DIR}"

echo "3) Starting container ${CONTAINER_NAME}..."
docker run -d --name "${CONTAINER_NAME}" -p 8080:8080 "${IMAGE_NAME}"

echo "4) Waiting for service to be ready at ${STARTUP_URL} (timeout ${STARTUP_TIMEOUT}s)..."
end_time=$((SECONDS + STARTUP_TIMEOUT))
while true; do
  if curl --silent --fail "${STARTUP_URL}" >/dev/null 2>&1; then
    echo "Service is up."
    break
  fi
  if (( SECONDS >= end_time )); then
    echo "Timed out waiting for service. Showing container logs:"
    docker logs "${CONTAINER_NAME}" || true
    exit 1
  fi
  sleep "${STARTUP_INTERVAL}"
done

echo "5) Running client tests (mvn test)..."
pushd "${CLIENT_DIR}" >/dev/null
mvn test
TEST_RC=$?
popd >/dev/null

if [ "${TEST_RC}" -ne 0 ]; then
  echo "Tests failed (rc=${TEST_RC}). Container logs:"
  docker logs "${CONTAINER_NAME}" || true
fi

exit "${TEST_RC}"
