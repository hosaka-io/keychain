pipeline {
    agent {
        docker {
            image 'leiningen' 
            args '-v /srv/docker/var/m2:/root/.m2 --dns=10.1.0.1'
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