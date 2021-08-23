FROM gradle:7.1.0-jdk16 as builder

# Copy local code to the container image.
WORKDIR /app
COPY build.gradle .
COPY gradle.properties .
COPY settings.gradle .
COPY src ./src

# Fix ownership of folders
# This changes default user to root
USER root
# This changes ownership of folder
RUN chown -R gradle /app
# This changes the user back to the default user "gradle"
USER gradle

# Build a release artifact.
RUN gradle build

# Use AdoptOpenJDK for base image.
# It's important to use OpenJDK 8u191 or above that has container support enabled.
# https://hub.docker.com/r/adoptopenjdk/openjdk8
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM adoptopenjdk/openjdk16:alpine-slim

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/build/libs/client-sajari-service*.jar /client-sajari-service.jar

# Run the web service on container startup.
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/client-sajari-service.jar"]