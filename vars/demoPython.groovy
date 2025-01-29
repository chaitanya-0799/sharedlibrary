def call( String dockerCred = 'a' ,String githubURL = 'a', String gitBranch = 'a', String dockerImage = 'a', String dockerTag = 'a', String containerName = 'a') {

    environment {
        dockerCred = "${dockerCred}"
        githubURL =  "${githubURL}"
        gitBranch = "${gitBranch}"
        dockerImage = "${dockerImage}"
        dockerTag = "${dockerTag}${BUILD_NUMBER}" 
        containerName = "${containerName}"
    }
    pipeline {
    agent any
    stages {
        stage('Gitcheckout'){
            steps {
                git branch: "${gitBranch}", url: "${githubURL}"
            }
        }
        stage('Build'){
            steps {
                sh 'docker build -t "${dockerImage}" -f build/Dockerfile .'
            }
        }

        stage('push') {
            steps {
                withCredentials([usernameColonPassword(credentialsId: 'dockerCred', variable: '')]) {
                }
            }
        }

        
        stage('Deploy') {
            steps {
                sh 'docker stop "${containerName}" || true'
                sh 'docker rm "${containerName}" || true'
                sh 'docker run -itdp 800:5000 --name "${containerName}" "${dockerImage}:${dockerTag}"'
            }
        }
    }
}
}
