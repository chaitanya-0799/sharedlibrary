def call( String dockerCred, String githubURL, String gitBranch, String dockerfilePath, 
          String dockerImage, String docTag, String containerName, String containerPort, String applicationPort ) {
    // Parameter validation
    if (!dockerCred || !githubURL || !gitBranch || !dockerfilePath || !dockerImage || !docTag || !containerName || !containerPort || !applicationPort) {
        error "Missing required parameters"
    }

    pipeline {
        environment {
            DOCKER_CRED = "${dockerCred}"
            GITHUB_URL = "${githubURL}"
            GIT_BRANCH = "${gitBranch}"
            PATH_FILE = "${dockerfilePath}"
            DOCKER_IMAGE = "${dockerImage}"
            DOCKER_TAG = "${docTag}${BUILD_NUMBER}" 
            CONTAINER_NAME = "${containerName}"
            CONT_PORT = "${containerPort}"
            APP_PORT = "${applicationPort}"
        }

        agent any

        stages {
            stage('Git Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build Docker Image') {
                steps {
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -f ${PATH_FILE} ."
                }
            }

            stage('Push to Docker Registry') {
                steps {
                    script {
                        docker.withRegistry('https://index.docker.io/v1/', DOCKER_CRED) {
                            sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        }
                    }
                }
            }

            stage('Deploy Container') {
                steps {
                    script {
                        sh """
                        docker ps -q --filter name=${CONTAINER_NAME} | grep -q . && docker stop ${CONTAINER_NAME} || true
                        docker ps -aq --filter name=${CONTAINER_NAME} | grep -q . && docker rm ${CONTAINER_NAME} || true
                        docker run -d -p ${CONT_PORT}:${APP_PORT} --name ${CONTAINER_NAME} ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }
                }
            }
        }
    }
}
