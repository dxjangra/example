pipeline {
    agent any
    //agent { label 'linuxnode' }

    stages {
        stage('Checkout') {
            steps {
                echo 'checkout from git'
            }
        }
        stage('Build') {
            steps {
                echo 'Buid by Jenkinsfile '
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
    }
}
