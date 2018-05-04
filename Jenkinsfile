pipeline {
    agent {
        docker {
            image 'leiningen' 
        }
    }
    stages {
        stage('Build') { 
            steps {
                sh 'lein uberjar'
            }
        }
    }
}