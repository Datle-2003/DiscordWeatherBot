# Base image
FROM openjdk:21-jdk-slim

# Working directory
WORKDIR /app

# Copy the fat JAR file to the container
ARG JAR_FILE=build/libs/discord-bot.jar
COPY ${JAR_FILE} app.jar

# Copy application.properties to the container
COPY src/main/resources/application.properties /application.properties

# Run the fat JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
