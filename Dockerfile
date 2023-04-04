FROM gradle:latest AS build
WORKDIR /app

COPY build.gradle.kts gradle.properties settings.gradle.kts /app/
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

COPY ./ /app/
RUN gradle shadowJar --no-daemon


FROM openjdk:17.0.2-slim

EXPOSE 8080
RUN mkdir /app
RUN ls /app
COPY --from=build /app/build/libs/open-ai-chatgpt.jar /app/application.jar

ENTRYPOINT ["java","-jar","/app/application.jar"]