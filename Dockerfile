FROM gradle:7-jdk17 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle buildFatJar --no-daemon

FROM openjdk:17

EXPOSE 8084

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/

ENTRYPOINT ["java", "-jar", "/app/ddns-updater.jar"]