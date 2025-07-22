pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile.ci'
            dir '.'
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
                sh 'docker --version && docker compose version'
                sh 'mvn clean verify -Dspring.profiles.active=compose -Dstyle.color=always -B -ntp'
           }
       }
    }
}
