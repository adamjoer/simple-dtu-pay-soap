#!/bin/bash
set -e

# =============================================================================
# Environment Variables
# =============================================================================
IMAGE_NAME='dtu-pay-jvm'
CONTAINER_NAME='dtu-pay'
SERVICE_DIR='service'
CLIENT_DIR='client'
DOCKERFILE="${SERVICE_DIR}/src/main/docker/Dockerfile.jvm"
TEST_PORT='8080'
PROD_PORT='80'

# Credential - must be set in environment
if [[ -z "${SIMPLE_DTU_PAY_API_KEY}" ]]; then
    echo "Error: SIMPLE_DTU_PAY_API_KEY environment variable is not set"
    exit 1
fi

# =============================================================================
# Helper Functions
# =============================================================================
info() {
    echo -e "\033[1;34m[INFO]\033[0m $*"
}

error() {
    echo -e "\033[1;31m[ERROR]\033[0m $*" >&2
}

success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $*"
}

cleanup_test_container() {
    info "Cleaning up test container..."
    docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true
}

# =============================================================================
# Stage: Build
# =============================================================================
stage_build() {
    info "========== Stage: Build =========="

    info "Building Maven package..."
    (cd "${SERVICE_DIR}" && mvn package)

    info "Building Docker image ${IMAGE_NAME}:${BUILD_NUMBER}..."
    docker build -f "${DOCKERFILE}" -t "${IMAGE_NAME}:${BUILD_NUMBER}" "${SERVICE_DIR}"

    success "Build stage completed"
}

# =============================================================================
# Stage: Test
# =============================================================================
stage_test() {
    info "========== Stage: Test =========="

    # Set up cleanup trap for this stage
    trap 'test_cleanup $?' RETURN

    info "Starting test container..."
    docker run -d \
        --name "${CONTAINER_NAME}" \
        -p "${TEST_PORT}:8080" \
        -e "SIMPLE_DTU_PAY_API_KEY=${SIMPLE_DTU_PAY_API_KEY}" \
        "${IMAGE_NAME}:${BUILD_NUMBER}"

    info "Waiting for service to be ready (timeout: 60s)..."
    local timeout=60
    local elapsed=0
    local interval=2

    while [[ $elapsed -lt $timeout ]]; do
        if curl --silent --fail "http://localhost:${TEST_PORT}/q/health/ready" > /dev/null 2>&1; then
            info "Service is ready!"
            break
        fi
        sleep $interval
        elapsed=$((elapsed + interval))
        echo -n "."
    done
    echo

    if [[ $elapsed -ge $timeout ]]; then
        error "Timeout waiting for service to be ready"
        return 1
    fi

    info "Running client tests..."
    (cd "${CLIENT_DIR}" && mvn test)

    success "Test stage completed"
}

test_cleanup() {
    local exit_code=$1

    if [[ $exit_code -ne 0 ]]; then
        error "Test stage failed - showing container logs:"
        docker logs "${CONTAINER_NAME}" 2>/dev/null || true
    fi

    cleanup_test_container
}

# =============================================================================
# Stage: Deploy
# =============================================================================
stage_deploy() {
    info "========== Stage: Deploy =========="

    info "Tagging image as latest..."
    docker tag "${IMAGE_NAME}:${BUILD_NUMBER}" "${IMAGE_NAME}:latest"

    info "Removing old production container..."
    docker rm -f "${CONTAINER_NAME}-prod" 2>/dev/null || true

    info "Starting production container..."
    docker run -d \
        --name "${CONTAINER_NAME}-prod" \
        -p "${PROD_PORT}:8080" \
        -e "SIMPLE_DTU_PAY_API_KEY=${SIMPLE_DTU_PAY_API_KEY}" \
        "${IMAGE_NAME}:latest"

    success "Deploy stage completed"
}

# =============================================================================
# Main
# =============================================================================
main() {
    info "Starting pipeline (Build #${BUILD_NUMBER})"

    stage_build
    stage_test
    stage_deploy

    success "Pipeline completed successfully!"
}

main "$@"