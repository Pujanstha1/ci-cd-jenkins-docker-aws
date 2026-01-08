pipeline {
    agent any
    parameters {
        string(
            name: 'SERVER_IP', 
            description: "Enter the Server Public IP"
        )
    }
    environment {
        //SSH_KEY = credentials('SSH_KEY')
        DOCKER_HUB_PASSWORD = credentials('DOCKER_HUB_TOKEN')
        SERVER_USER = 'ubuntu'
        DOCKER_HUB_USER = 'pujan240'
        DOCKER_HUB_REPO = 'crud_app'
        TAG = 'latest'
    }

    stages {
        stage ('Build an Image and Push it to DockerHub') {

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

        stage ('Deploy to EC2') {
            withCredentials([
                string(credentialsId: "SSH_KEY", variable:"SSH_KEY_FILE")
            ]) {
                sh '''
                    set -e
                    mkdir -p ~/.ssh
                    chmod 700 ~/.ssh
                    echo -e "Host *\n\tStrictHostKeyChecking no\n" > ~/.ssh/config
                    chmod 600 ~/.ssh/config

                    echo "$SSH_KEY" | base64 -d >> mykey.pem
                    chmod 400 mykey.pem 
                    touch ~/.ssh/known_hosts 
                    ssh-keygen -R "$SERVER_IP"

                    scp -i mykey.pem ./docker-compose.yaml "$SERVER_NAME"@"SERVER_IP:~/"

                    ssh -i mykey.pem "$SERVER_USER"@"$SERVER_IP" " 
                    docker compose --env-file ./.env/dev_env pull 
                    docker compose --env-file ./.env/dev_env down 
                    docker compose --env-file ./.env/dev_env up -d 
                    "
                '''
            }
        }
    }
}
