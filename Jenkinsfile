pipeline {
    agent any
//     agent {
//         dockerfile {
//             filename 'Dockerfile.ci'
//             dir '.'
//             args '''
//                 -v /var/run/docker.sock:/var/run/docker.sock
//                 -u root:root
//             '''
//         }
//     }
    options {
       timeout(time: 10, unit: 'MINUTES')
       ansiColor('xterm')
    }
   // triggers {
   //     cron('H/5 * * * *')
   // }
    environment {
        PROJECT_NAME = "${BUILD_TAG}"
    }
    stages {
       stage('Setup') {
           steps {
                sh '''
                    docker system df
                    docker compose -f compose-ci.yaml --project-name ${PROJECT_NAME} up -d
                    docker compose -f compose-ci.yaml --project-name ${PROJECT_NAME} ps
                '''
           }
       }
       stage('Integration Test') {
           steps {
                sh 'docker --version && docker compose version'
                sh '''
                    docker compose -f compose-ci.yaml --project-name ${PROJECT_NAME} exec -T maven mvn clean verify \
                       -Dspring.profiles.active=compose-ci \
                       -Dstyle.color=always \
                       -B -ntp
                '''
           }
       }
        stage('Cleanup') {
            steps {
                sh '''
                    docker compose -f compose-ci.yaml --project-name ${PROJECT_NAME} down --rmi all --volumes --remove-orphans
                    docker builder prune -af
                    docker system df
                '''
                cleanWs()
            }
        }
    }
}
