pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'
            args '''
                -v /var/run/docker.sock:/var/run/docker.sock
                -u root:root
            '''
        }
    }
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
                sh 'whoami'
                sh 'apt-get update -qq && apt-get install -y -qq docker.io docker-compose-plugin'
                sh 'docker --version && docker compose version'
                sh 'mvn clean verify -Dspring.profiles.active=compose -Dstyle.color=always -B -ntp'
           }
       }
    }
}
