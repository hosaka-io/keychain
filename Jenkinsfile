pipeline {

    def app

    agent {
        docker {
            image 'leiningen' 
            args '-v /srv/docker/var/m2:/root/.m2'
        }
    }
    stages {
        stage('Build JAR') { 
            steps {
                sh 'lein do clean, uberjar'
            }
        }
        stage('Build image') {
            app = docker.build("registry.i.hosaka.io/keychain")
        }
        stage('Push image') {
            docker.withRegistry('https://registry.i.hosaka.io') {
                app.push("${env.BUILD_NUMBER}")
            }
        }
    }
}