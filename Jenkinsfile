pipeline {

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
            steps {
                script {
                    withDockerServer(){
                    withDockerRegistry('https://registry.i.hosaka.io') {
                        def app = docker.build("registry.i.hosaka.io/keychain")
                        app.push("${env.BUILD_NUMBER}")
                    }}
                }
            }
        }
    }
}