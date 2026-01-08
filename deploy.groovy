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
        // stage ('Build an Image and Push it to DockerHub') {

        //     steps {

        //         withCredentials([
        //         string(credentialsId: "DOCKER_HUB_PASSWORD", variable: "DOCKER_HUB_PASSWORD")
        //         ]) {
        //             sh '''
        //                 set -e
        //                 echo "$DOCKER_HUB_PASSWORD" | docker login -u $DOCKER_HUB_USER --password-stdin
        //                 docker build -t "$DOCKER_HUB_USER/$DOCKER_HUB_REPO:$TAG" .
        //                 docker push "$DOCKER_HUB_USER/$DOCKER_HUB_REPO:$TAG"
        //             '''
        //             }
        //     }
            
        // }

        stage ('Deploy to EC2') {

            steps {
                withCredentials([
                    file(credentialsId: "SSH_KEY_FILE", variable:"SSH_KEY")
                ]) {
                    sh '''
                        set -e
                        mkdir -p ~/.ssh
                        chmod 700 ~/.ssh

                        touch ~/.ssh/known_hosts 
                        ssh-keygen -R "$SERVER_IP"

                        scp -i "$SSH_KEY" -o StrictHostKeyChecking=no \
                         ./docker-compose.yaml $SERVER_USER@$SERVER_IP:~/ 

                        ssh -i "$SSH_KEY" \
                        -o StrictHostKeyChecking=no \
                        $SERVER_USER@$SERVER_IP " 
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
