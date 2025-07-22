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
    stages {
       stage('Integration Test') {
           steps {
                sh 'docker --version && docker compose version'
                sh '''
                    export WORKSPACE=${WORKSPACE}
                    export BUILD_TAG=${BUILD_TAG}

                    docker compose -f compose-ci.yaml --project-name=${BUILD_TAG} up -d
                    docker compose -f compose-ci.yaml --project-name=${BUILD_TAG} ps

                    docker compose -f compose-ci.yaml --project-name=${BUILD_TAG} exec -T maven mvn clean verify \
                        -Dspring.docker.compose.enabled=false \
                        -DMATHJS_API_URL=http://wiremock:8080/v4/ \
                        -Dstyle.color=always \
                        -B -ntp
                '''
           }
       }
    }
}
