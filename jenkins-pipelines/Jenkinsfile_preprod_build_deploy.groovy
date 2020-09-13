node {
    def app

    stage('Clone repository') {
        /* Let's make sure we have the repository cloned to our workspace */

        checkout([$class: 'GitSCM', branches: [[name: '${BRANCH}']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/miscaandrei/profileAPI.git']]])
    }


    stage("SonarQube QA analysis") {
        withSonarQubeEnv('SonaQube-Server'){
            def gradleTool =  tool "GradleTool"
            def scannerTool = tool "SonarScannerTool"
            
            bat "${gradleTool}/bin/gradle microBundle" //run a gradle task
            bat "${scannerTool}/bin/sonar-scanner -Dsonar.projectKey=profileAPI -Dsonar.sources=src/main/resources,src/main/webapp,src/main/java -Dsonar.java.binaries=. "
        }
    }
    // stage("Quality QA Gate") {
    //     timeout(time: 2, unit: "MINUTES") {
    //         def qg = waitForQualityGate()
    //         if (qg.status != 'OK') {
    //             // error "Pipeline aborted due to quality gate failure: ${qg.status}";
    //             unstable("The SonarQube scan did not pass!")
    //         }
    //     }
    // }

    stage('Build image') {
        /* This builds the actual image; synonymous to
         * docker build on the command line */

        app = docker.build("miscaandrei/apigradle:$SEMANTIC_VERSION", "--build-arg BRANCH=${BRANCH} -f Dockerfile .")
    }


    stage('Push image') {
        /* Finally, we'll push the image with two tags:
         * First, the incremental build number from Jenkins
         * Second, the 'latest' tag.
         * Pushing multiple tags is cheap, as all the layers are reused. */
        docker.withRegistry('https://registry.hub.docker.com', 'dockerhub') {
            app.push("${SEMANTIC_VERSION}")
            app.push("latest")
        }
    }

    stage('Deploy') {
           openshift.withCluster() {
                openshift.withProject("project-futurama-prod") {
                    openshift.selector("dc", "profileAPI-prod").rollout().latest()
                    def latestDeploymentVersion = openshift.selector('dc',"profileAPI-prod").object().status.latestVersion
                    def rc = openshift.selector('rc', "profileAPI-prod-${latestDeploymentVersion}")
                    timeout (time: 10, unit: 'MINUTES') {
                        rc.untilEach(1){
                            def rcMap = it.object()
                            return (rcMap.status.replicas.equals(rcMap.status.readyReplicas))
                        }
                    }
                }
             echo "Deploy Finished"
         }
    }
}



