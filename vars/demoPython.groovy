def call( String dockerCred = 'a' ,String githubURL = 'a', String gitBranch = 'a', String dockerImage = 'a', \
    String docTag = 'a', String containerName = 'a', String containerPort = 'a', String applicationPort = 'a' ) {

    
    pipeline {
    environment {
        DOCKERCRED = "${dockerCred}"
        GITHUBURL =  "${githubURL}"
        GITBRANCH = "${gitBranch}"
        DOCKERIMAGE = "${dockerImage}"
        DOCKERTAG = "${docTag}${BUILD_NUMBER}" 
        CONTAINERNAME = "${containerName}"
        CONTPORT = "${containerPort}"
        APPPORT = "${applicationPort}"
    }
        agent any
        stages {
            stage('Gitcheckout'){
                steps {
                    git branch: "${GITBRANCH}", url: "${GITHUBURL}"
                }
            }
            stage('Build'){
                steps {
                    sh "docker build -t ${DOCKERIMAGE}:${DOCKERTAG} -f build/Dockerfile ."
                }
            }

            stage('push') {
                steps {
                    withDockerRegistry(credentialsId: 'docker', url: 'https://index.docker.io/v1/') {
                        sh "docker push ${DOCKERIMAGE}:${DOCKERTAG}"
                    }
                }
            }

            
            stage('Deploy') {
                steps {
                    sh "docker stop ${CONTAINERNAME} || true"
                    sh "docker rm ${CONTAINERNAME} || true"
                    sh "docker run -itdp ${CONTPORT}:${APPPORT} --name ${CONTAINERNAME} ${DOCKERIMAGE}:${DOCKERTAG}"
                }
            }
        }
    }
}
