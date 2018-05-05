pipeline {
    agent {
        docker {
            image 'leiningen' 
            args '-v /srv/docker/var/m2:/root/.m2'
        }
    }
    stages {
        stage('Build') { 
            steps {
                sh 'lein do clean, uberjar'
            }
        }
    }
}