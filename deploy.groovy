pipeline {
    agent any
    parameters {
        string(
            name: 'SERVER_IP', 
            description: "Enter the Server Public IP"
        )
    }
    environment {
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

            steps {
                withCredentials([
                    file(credentialsId: "SSH_KEY_FILE", variable:"SSH_KEY")
                ]) {
                    sh '''
                        set -e
                        mkdir -p ~/.ssh
                        chmod 700 ~/.ssh
                        echo -e "Host *\n\tStrictHostKeyChecking no\n" > ~/.ssh/config
                        chmod 600 ~/.ssh/config

                        // These two lines are not required as we upload the .pem file itself and assume that we don't need to change the chmod
                        // echo "$SSH_KEY" | base64 -d >> mykey.pem
                        // chmod 400 mykey.pem 
                        touch ~/.ssh/known_hosts 
                        ssh-keygen -R "$SERVER_IP"

                        scp -i $SSH_KEY ./docker-compose.yaml "$SERVER_NAME"@"SERVER_IP:~/"

                        ssh -i $SSH_KEY "$SERVER_USER"@"$SERVER_IP" " 
                        docker compose --env-file ./.env/dev_env pull 
                        docker compose --env-file ./.env/dev_env down 
                        docker compose --env-file ./.env/dev_env up -d 
                        "
                    '''
                }
            }    
        }
    }
}
