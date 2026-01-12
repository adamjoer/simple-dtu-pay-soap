pipeline {
    agent any

    environment {
        IMAGE_NAME = 'dtu-pay-jvm'
        CONTAINER_NAME = 'dtu-pay'
        SERVICE_DIR = 'service'
        CLIENT_DIR = 'client'
        DOCKERFILE = "${SERVICE_DIR}/src/main/docker/Dockerfile.jvm"
        TEST_PORT = '8080'
        PROD_PORT = '80'
    }

    options {
        ansiColor('xterm')
    }

    stages {
        stage('Build') {
            steps {
                dir(SERVICE_DIR) {
                    sh 'mvn package'
                }

                sh "docker build -f ${DOCKERFILE} -t ${IMAGE_NAME}:${BUILD_NUMBER} ${SERVICE_DIR}"
            }
        }

        stage('Test') {
            steps {
                sh "docker run -d --name ${CONTAINER_NAME} -p ${TEST_PORT}:8080 ${IMAGE_NAME}:${BUILD_NUMBER}"

                timeout(time: 60, unit: 'SECONDS') {
                    waitUntil {
                        script {
                            def exitCode = sh(
                                script: "curl --silent --fail http://localhost:${TEST_PORT}/q/health/ready",
                                returnStatus: true
                            )
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
                sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"

                sh "docker rm -f ${CONTAINER_NAME}-prod || true"
                sh "docker run -d --name ${CONTAINER_NAME}-prod -p ${PROD_PORT}:8080 ${IMAGE_NAME}:latest"
            }
        }
    }
}
