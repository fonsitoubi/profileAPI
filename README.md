
How to run

```
gradle microBundle
docker-compose up --build
```
Then access

HealthAPI
```
http://localhost:8080/health
```

OpenAPI
```
http://localhost:8080/openapi
```

Swagger UI
```
http://localhost:3000/?url=http://localhost:8080/openapi
```

## Things to be improved
As I was into this tech test I realized that the information is quite poor when it come to running/build the java application, at least there should be a requirements.txt that specifies the gradle and java version to use because when I used the latest gradle version I got a dependency warning.

Also as I mentioned in the previous interview I don't have any solid Java knowledge about programing and also with no information about the behavior of the microservice planning or writing any unit test was impossible for me, I've tried to implement a basic Java unit test bu that implied to modify the source code of the app that I have no knowledge about and also is not of my responsibility since my duty is to ensure the developing team has all the tools to do their work. But to try and compansate for my lack of knowledge I've implemented a sonar instance to do a static analysis of the code using the comunity standard profiles, the result of this you can find it in the SonarScanResults folder. 

I've done most of the work on my Windows 10 home pc so there are some windows sommands on the jenkins pipelines such as bat instead of sh, so keep that in mind when running the Jenkins pipelines.

As a extra I've screen recorded and explained all the parts related to the quick local development and 1 click deployment and uploaded it to YouTube on my channel as a private video that once I get confirmation that you finish reviewing everything I will remove the video and github repository.

YouTube Video will be attached in an email.




## Questionary

1. Share your thoughts on deploying our API into Production:
    a. What solutions would you use to automate the whole process? Explain why with pros/cons
    From my experience and as I did on my local workstation my choices would be:
        - Jenkins instance (with slave instances) to execute all the pipelines either manually triggered by a user or automatically triggered by a git commit or cronjob, because it has a lot of compatible plugins and community driven to constantly evolve in a better version. A few of these plugins are designed to directly connect service like SonarQube, Jfrog, Junit (for running unit tests), CucumberReports, cloud providers connectors (AWS, AZure, GCP, Openshift)
        - Sonarqube with a single instance to run all the static analysis and security vulnerabilities scans, cons would be more financial point because a paid version offer more solution than the free one
        - Jfrog (paid version) as a binary repository for the Docker images and also the java dependencies because of the Xray feature that scans all your packages to review if it's dependencies have any security issues and also because you can use it as a proxy when searching for libraries that way having a local version of your dependencies in case the source is not available.
        - Openshift as a docker orchestration platform since it has the core functions of Kubernetes but offers a more friendly and detailed web UI that's a big help when you need to quick review performances of the pods and recent errors. 
    b. What autoscaling solution would you implement knowing that the API can easily reach 50K daily users? Again, explain why with pros/cons.
        Apart from defining a autoscaling on the deployment in charge of that microservice I would also define a autoscaling for it's dependent services (such as the database) by defining a trigger average on the memory usage or the cpu usage (should ask the dev team if the microservice is more cpu demanding or memory) and defining a minimum of available replicas. Also, by doing a multistage build of the java app we reduce the start time of the new pods reducing the "overload" time of the current running instances. 
        And for the database/s I would recommend implementing a shared or replication cluster.

2. The REST API of the exercise is a piece of a complex client/server application which is composed of several microservices. A microservice is composed of packages. A package can depend on other packages. Packages use semantic versioning. Versions of client and server packages of the same microservice need to be in sync. Client and server packages are stored in GitLab. The client is composed of Unity packages (UPM) along with native packages and it is built for several platforms (iOS, Android, Windows, MacOS). Given this context describe:
    a. How you would model a robust and efficient CI/CD pipeline that:
        i. Guarantees packages integrity
            By using a binary repository (Jfrog/Nexus) for storing both the external/internal library/packages and forcing everyone (developers, QA, automated systems) package manager to attack that only repository that way we can guarantee that everyone uses the same versions
        ii. Guarantees that client and server packages of the same microservice are in sync
        iii. Keeps packages up to date automatically
        iv. Let anyone (QA testers, designers, producers, programmers) build the application for a given platform out of a given branch by hitting a button.
As the shown before we can create a build with parameters job in Jenkins that runs everything from code analysis, gradle compiling and docker build up to publishing the image and deploying it to the pre-production environment. In the example showed we can leave a branch input option so that the user can choose to manually input the desired branch to build or in case of the developers we can do a multibranch pipeline job that when a developer pushed a new change the system automatically does these actions for him. 
Also there must be a permission system put in place where the developers can build new docker images (version x.y ) of the desired app but the QA has the option to elevate that version to a production candidate.
Quick explanation: developers build a docker image of the version 1.2.1 that they are working on and the QA uses that same docker image to do the validation for production and only when they validate as OK we deploy it to production. In case the QA request any type of correction to be done by the developing team the new image will be 1.2.1-1 (or the desired semantic marking) that way when we deploy to production we are sure the deployed version is correct, by overwriting the 1.2.1 version they may be issue and some instances are not updated correctly.
        v. Upon merging a package's feature branch into master automatically builds and validates the package along with all packages that depend on it as well as the application for all platforms.
    We can create a Jenkins job with a git trigger related to the master branch that when it detects a new change in the branch it automatically builds and deploy to preproduction or integration environment. The default job behavior does the Sonar Scan for security issues and dependencies. 
    b. What tests and analysis would you perform along the pipeline to guarantee the quality of the packages and the application?
    As said earlier I would use the standard SonarQube profiles and quality gates for Java, XML, HTML etc. . . but we could also create our own from scrap or by modifying existing one to ensure we deliver what our company/team best quality.

