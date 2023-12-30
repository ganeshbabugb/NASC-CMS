pipeline {
    agent any

    tools {
<<<<<<<<< Temporary merge branch 1
        // Define Maven tool with version 3.9.6
=========
>>>>>>>>> Temporary merge branch 2
        maven 'maven_3_9_6'
    }

    stages {
        stage('Build Maven') {
            steps {
                // Checkout the main branch from the Git repository
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/ganeshbabugb/NASC-CMS']])

                // Download license-checker-machineid and run it
                sh 'mvn dependency:get -Dartifact=com.vaadin:license-checker-machineid:1.8.1 && java -jar ~/.m2/repository/com/vaadin/license-checker-machineid/1.8.1/license-checker-machineid-1.8.1.jar'

                // Clean and package the Maven project with production profile
                sh 'mvn clean package -Pproduction -Dvaadin.force.production.build=true'
            }
        }
<<<<<<<<< Temporary merge branch 1
    }

   stage('Deploy') {
        steps {
            // Set environment variables for deployment
            withEnv(['DB_USERNAME=admin', 'DB_PASSWORD=password', 'PORT=8081']) {
                sh 'echo "Deploying the application"'
                sh 'java -jar -Dspring.profiles.active=prod target/nasc-cms-application.jar'
            }
        }
    }

=========

        stage('Deploy') {
            steps {
                // Set environment variables for deployment
                withEnv(['DB_USERNAME=admin', 'DB_PASSWORD=password', 'PORT=8081']) {
                    // Display a message indicating the deployment is starting
                    sh 'echo "Deploying the application"'

                    // Run the Java application with specified parameters
                    sh 'java -jar -Dspring.profiles.active=prod target/nasc-cms-application.jar'
                }
            }
        }
    }
>>>>>>>>> Temporary merge branch 2
}
