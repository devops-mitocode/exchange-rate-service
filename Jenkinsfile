pipeline {
   agent none
   options {
       timeout(time: 10, unit: 'MINUTES')
       ansiColor('xterm')
   }
   // triggers {
   //     cron('H/5 * * * *')
   // }
   stages {
       stage('Integration Test') {
           agent {
               docker {
                   image 'maven:3.9.6-eclipse-temurin-17-alpine'
               }
           }
           steps {
               sh 'mvn clean verify -Dspring.profiles.active=compose -Dstyle.color=always -B -ntp'
           }
       }
//        stage('Integration Test') {
//            agent {
//                docker {
//                    image 'maven:3.8.8-eclipse-temurin-17-alpine'
//                    args "--network ${BUILD_TAG}_default -e CONTAINER_IP=${CONTAINER_IP}"
//                }
//            }
//            options {
//                skipDefaultCheckout()
//            }
//            steps {
//                sh 'sleep 1m'
//                sh "curl http://${CONTAINER_IP}:9966/petclinic/api/pettypes"
//
//
//                dir('integration-tests'){
//                    git branch: 'master',
//                    url: 'https://github.com/devops-mitocode/integration-tests.git'
//                }
//                sh 'mvn clean verify -Dstyle.color=always -f integration-tests/pom.xml -B -ntp'
//
//
//                publishHTML(
//                    target: [
//                        reportName : 'Serenity Report',
//                        reportDir:   'acceptance-it/target/site/serenity',
//                        reportFiles: 'index.html',
//                        keepAll:     true,
//                        alwaysLinkToLastBuild: true,
//                        allowMissing: false
//                    ]
//                )
//            }
//        }
   }
}
