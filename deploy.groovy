pipeline {
    agent any
    parameters {
        string(
            name: 'SERVER_IP', 
            description: "Enter the Server Public IP"
        )
    }
    environment {
        SSH_KEY = credentials('SSH_KEY')
        DOCKER_HUB_PASSWORD = credentials('DOCKER_HUB_TOKEN')
        SERVER_USER = 'ubuntu'
        DOCKER_HUB_USER = 'pujan240'
        DOCKER_HUB_REPO = 'crud_app'
        TAG = 'latest'
    }

    stages {
        stage ('Build an Image') {

            steps {

                withCredentials([
                string(credentialsId: "DOCKER_HUB_PASSWORD", variable: "DOCKER_HUB_PASSWORD")
                ]) {
                    sh '''
                        set -e
                        echo "$DOCKER_HUB_PASSWORD" | docker login
                        docker build -t "$DOCKERHUB_USER/$DOCKER_HUB_REPO:$TAG"
                        docker push "$DOCKER_HUB_USER/$DOCKER_HUB_REPO:$TAG"
                    '''
                    }
            }
            
        }
    }
}