def call( String dockerCred = 'a' ,String githubURL = 'a', String gitBranch = 'a', String dockerImage = 'a', String docTag = 'a', String containerName = 'a') {

    
    pipeline {
    environment {
        dockerCred = "${dockerCred}"
        githubURL =  "${githubURL}"
        gitBranch = "${gitBranch}"
        dockerImage = "${dockerImage}"
        dockerTag = "${docTag}${BUILD_NUMBER}" 
        containerName = "${containerName}"
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
                    sh "docker build -t ${dockerImage} -f build/Dockerfile ."
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
                    sh "docker run -itdp 800:5000 --name ${containerName} ${dockerImage}:${dockerTag}"
                }
            }
        }
    }
}
