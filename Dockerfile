FROM gradle:latest AS builder
# this is multi-stage build intented to reduce the final image total size


ARG BRANCH=develop
ARG REPO=https://github.com/miscaandrei/profileAPI.git


RUN mkdir -m 700 /root/.ssh; \
  touch -m 600 /root/.ssh/known_hosts; \
  ssh-keyscan github.com > /root/.ssh/known_hosts

RUN git clone -b ${BRANCH} ${REPO} src


RUN ls /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle microBundle --no-daemon 



FROM openjdk:11

COPY --from=builder /home/gradle/src/build/libs/*.jar /opt/application.jar

EXPOSE 8080

CMD java -jar /opt/application.jar