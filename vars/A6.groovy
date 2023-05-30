node {
    stage('checkout') {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[url: 'https://github.com/opstree/spring3hibernate.git']]])
    }

    stage('build') {
        sh 'mvn package'
    }

    stage('parallel stage') {
        def skipTests = false
        if (params.Skip_tests == 'Do_not') {
            skipTests = true
        }

        parallel (
            'code stability': {
                if (!skipTests) {
                    sh 'mvn surefire:test'
                }
            },
            'Code QA': {
                sh 'mvn cobertura:cobertura'
            },
            'Coverage analysis': {
                recordIssues(tools: [checkStyle(pattern: '**/checkstyle-result.xml')])
            }
        )
    }

    stage('Report generation of code QA & Analysis') {
        cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/target/site/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
    }

    stage('Publish the artifact') {
        input(message: "Do you want to publish the artifact?", ok: "yes")
        archiveArtifacts artifacts: 'target/*.jar, target/*.war', followSymlinks: false
    }

    // Post-build actions
    try {
        // Actions to perform always, regardless of build result
    } finally {
        // Send success notification
        echo "${env.BUILD_ID}"
        slackSend(channel: 'mohit-personal-use', message: "${env.BUILD_ID} successful")
        emailext attachLog: true, body: '''<p>Hi,</p>
            <p>The build ${env.JOB_NAME} has completed.</p>
            <p>Build Status: ${currentBuild.currentResult}</p>
            ''', recipientProviders: [buildUser()], replyTo: 'djmohit95@gmail.com', subject: "Jenkins Alert: ${env.JOB_NAME}", to: 'djmohit95@gmail.com'
    }
    
    // Failure handling
    catchError {
        // Send failure notification
        echo "${env.BUILD_ID}"
        slackSend(channel: 'mohit-personal-use', message: "${env.BUILD_ID} is Unsuccessful")
        emailext attachLog: true, body: '''<p>Hi,</p>
            <p>The build ${env.JOB_NAME} has completed.</p>
            <p>Build Status: ${currentBuild.currentResult}</p>
            ''', recipientProviders: [buildUser()], replyTo: 'djmohit95@gmail.com', subject: "Jenkins Alert: ${env.JOB_NAME}", to: 'djmohit95@gmail.com'
        throw new RuntimeException("Build failed") // Mark the build as failed
    }
}

