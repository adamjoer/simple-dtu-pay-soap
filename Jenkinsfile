pipeline {
    agent any

    environment {
        IMAGE_NAME = 'code-with-quarkus-jvm'
        CONTAINER_NAME = 'code-with-quarkus-test'
        SERVICE_DIR = 'service'
        CLIENT_DIR = 'client'
        DOCKERFILE = "${SERVICE_DIR}/src/main/docker/Dockerfile.jvm"
        PORT = '8080'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building service...'
                dir(SERVICE_DIR) {
                    sh 'mvn package'
                }

                echo 'Building Docker image...'
                sh "docker build -f ${DOCKERFILE} -t ${IMAGE_NAME}:${BUILD_NUMBER} ${SERVICE_DIR}"
            }
        }

        stage('Test') {
            steps {
                echo 'Starting container for testing...'
                sh "docker run -d --name ${CONTAINER_NAME} -p ${PORT}:${PORT} ${IMAGE_NAME}:${BUILD_NUMBER}"

                echo 'Waiting for service...'
                timeout(time: 60, unit: 'SECONDS') {
                    waitUntil {
                        script {
                            def exitCode = sh(
                                script: "curl --silent --fail http://localhost:${PORT}/q/health/ready",
                                returnStatus: true
                            ).trim()
                            return exitCode == 0
                        }
                    }
                }

                dir(CLIENT_DIR) {
                    sh 'mvn test'
                }
            }
            post {
                failure {
                    sh "docker logs ${CONTAINER_NAME} || true"
                }
                cleanup {
                    sh "docker rm -f ${CONTAINER_NAME} || true"
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Tagging image as latest...'
                sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"

                echo 'Deploying container...'
                sh "docker rm -f ${CONTAINER_NAME}-prod || true"
                sh "docker run -d --name ${CONTAINER_NAME}-prod -p ${PORT}:${PORT} ${IMAGE_NAME}:latest"
            }
        }
    }
}
