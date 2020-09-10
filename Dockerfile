FROM gradle:latest AS builder
# this is multi-stage build intented to reduce the final image total size


RUN mkdir -m 700 /root/.ssh; \
  touch -m 600 /root/.ssh/known_hosts; \
  ssh-keyscan github.com > /root/.ssh/known_hosts

RUN git clone -b develop https://github.com/miscaandrei/profileAPI.git src


RUN ls /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon --console=plain
RUN ls /home/gradle/src
RUN ls /home/gradle/src/build
RUN ls /home/gradle/src/build/libs/



FROM openjdk:11

COPY --from=builder /home/gradle/src/ROOT-microbundle.jar /opt/application.jar

EXPOSE 8080

CMD java -jar /opt/application.jar