FROM gradle:jdk15 AS build
COPY --chown=gradle:gradle build.gradle.kts gradle.properties settings.gradle.kts /home/gradle/
COPY --chown=gradle:gradle src /home/gradle/src
WORKDIR /home/gradle
RUN gradle docker --no-daemon

FROM openjdk:15-jdk-alpine
RUN mkdir /app
COPY --from=build /home/gradle/build/install/ParkeerAssistent/lib/*.jar /app/
COPY --from=build /home/gradle/build/libs/ParkeerAssistent-jvm-*.jar /app/
COPY apple-app-site-association.json /app/apple-app-site-association.json

CMD java -cp "/app/*" ServerKt
