pipeline {
    agent any

    environment {
        // Set SonarQube and GitHub credentials
        SONARQUBE_TOKEN = credentials('f2bfb920d317b79d27cb250456aa42c94099d44e') // Add this in Jenkins credentials
        SONAR_HOST_URL = 'https://sonarcloud.io'   // Replace with your SonarQube server URL
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Run SonarScanner') {
            when {
                // Ensure this stage only runs for PRs
                expression { return env.CHANGE_ID != null }
            }
            steps {
                script {
                    // Run SonarScanner with PR analysis parameters
                    sh """
                    sonar-scanner \
                    -Dsonar.projectKey=RXL-amitsharma1_testSIG7 \
                    -Dsonar.sources=.\
                    -Dsonar.host.url=https://sonarcloud.io \
                    -Dsonar.login=f2bfb920d317b79d27cb250456aa42c94099d44e \
                    -Dsonar.pullrequest.key=${env.CHANGE_ID} \
                    -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH} \
                    -Dsonar.pullrequest.base=main \
                    -Dsonar.pullrequest.github.repository=rxl-amitsharma1/testSIG7
                    """
                }
            }
        }


        stage('SonarQube Quality Gate') {
            steps {
                // Wait for SonarQube analysis and fail the build if the Quality Gate fails
                waitForQualityGate abortPipeline: true
            }
        }
    }
}
