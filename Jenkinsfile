pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile.ci'
            dir '.'
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
                sh 'docker --version && docker compose version'
//                 sh 'mvn clean verify -Dspring.profiles.active=compose -Dstyle.color=always -B -ntp'
//                 sh 'mvn clean verify -Dspring.profiles.active=compose -Dspring.docker.compose.file=compose-ci.yaml -DBUILD_TAG=abc -B -ntp'
                sh '''
                   echo "Ejecutando tests para: ${BUILD_TAG}"
                   echo "Workspace: ${WORKSPACE}"

                   # Levantar el stack completo (incluyendo Maven)
                   export WORKSPACE=${WORKSPACE}
                   export BUILD_TAG=${BUILD_TAG}

                   env | sort

                   docker compose -f compose-ci.yaml --project-name=${BUILD_TAG} up -d

                   # Verificar que servicios estén arriba
                   echo "Estado de servicios:"
                   docker compose -f compose-ci.yaml --project-name=${BUILD_TAG} ps

                   # Ejecutar Maven DENTRO del contenedor maven
                   docker compose -f compose-ci.yaml --project-name=${BUILD_TAG} exec -T maven mvn clean verify \
                       -Dspring.profiles.active=compose \
                       -Dstyle.color=always \
                       -B -ntp
                '''
           }
       }
    }
}
