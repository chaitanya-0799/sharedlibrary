def call( String dockerCred = 'a' ,String githubURL = 'a', String gitBranch = 'a', String dockerImage = 'a', String docTag = 'a', String containerName = 'a', String containerPort = 'a', String applicationPort = 'a' ) {

    
    pipeline {
    environment {
        dockerCred = "${dockerCred}"
        githubURL =  "${githubURL}"
        gitBranch = "${gitBranch}"
        dockerImage = "${dockerImage}"
        dockerTag = "${docTag}${BUILD_NUMBER}" 
        containerName = "${containerName}"
        contPort = "${containerPort}"
        appPort = "${applicationPort}"
    }
        agent any
        stages {
            stage('Gitcheckout'){
                steps {
                    git branch: "${gitBranch}", url: "${githubURL}"
                }
            }
            stage('Build'){
                steps {
                    sh "docker build -t ${dockerImage}:${dockerTag} -f build/Dockerfile ."
                }
            }

            stage('push') {
                steps {
                    withDockerRegistry(credentialsId: 'docker', url: 'https://index.docker.io/v1/') {
                        sh "docker push ${dockerImage}:${dockerTag}"
                    }
                }
            }

            
            stage('Deploy') {
                steps {
                    sh "docker stop ${containerName} || true"
                    sh "docker rm ${containerName} || true"
                    sh "docker run -itdp ${contPort}:${applicationPort} --name ${containerName} ${dockerImage}:${dockerTag}"
                }
            }
        }
    }
}
