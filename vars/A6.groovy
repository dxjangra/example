pipeline {
    agent any
    tools {
        maven 'maven'
    }
    parameters {
        choice(name: 'Skip_tests', choices: ['Skip', 'Do_not'], description: 'Select the options')
    }
    stages {
        stage('checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[url: 'https://github.com/opstree/spring3hibernate.git']]])
            }
        }
        stage('build') {
            steps {
                sh 'mvn package'
            }
        }
        stage('parallel stage') {
            when {
                expression { params.Skip_tests == 'Do_not' }
            }
            parallel {
                stage('code stability') {
                    steps {
                        sh 'mvn surefire:test'
                    }
                }
                stage('Code QA') {
                    steps {
                        sh 'mvn cobertura:cobertura'
                    }   
                }
                stage('Coverage analysis') {
                    steps {
                        recordIssues(tools: [checkStyle(pattern: '**/checkstyle-result.xml')])
                    }
                }
            }
        }
        stage('Report generation of code QA & Analysis') {
            steps {
                cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/target/site/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
            }
        }
        stage('Publish the artifact') {
            input {
                message "Do you want to publish the artifact?"
                ok "yes"
            }
            steps {
                archiveArtifacts artifacts: 'target/*.jar, target/*.war', followSymlinks: false
            }
        }
    }
    post {
        always {
            // Actions to perform always, regardless of build result
        }
        success {
            echo "${env.BUILD_ID}"
            slackSend(channel: 'mohit-personal-use', message: "${env.BUILD_ID} successful")
            emailext attachLog: true, body: '''<p>Hi,</p>
                <p>The build ${currentBuild.fullDisplayName} has completed.</p>
                <p>Build Status: ${currentBuild.result}</p>
                ''', recipientProviders: [buildUser()], replyTo: 'djmohit95@gmail.com', subject: 'Jenkins Alert: ${currentBuild.fullDisplayName}', to: 'djmohit95@gmail.com'
        }
        failure {
            echo "${env.BUILD_ID}"
            slackSend(channel: 'mohit-personal-use', message: "${env.BUILD_ID} is Unsuccessful")
            emailext attachLog: true, body: '''<p>Hi,</p>
                <p>The build ${currentBuild.fullDisplayName} has completed.</p>
                <p>Build Status: ${currentBuild.result}</p>
                ''', recipientProviders: [buildUser()], replyTo: 'djmohit95@gmail.com', subject: 'Jenkins Alert: ${currentBuild.fullDisplayName}', to: 'djmohit95@gmail.com'
        }
    }
}
