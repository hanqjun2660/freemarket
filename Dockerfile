# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

LABEL image.name="freemarket-api"

# Set the working directory to /app
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew .
COPY gradle gradle

# Copy the project files
COPY build.gradle .
COPY settings.gradle .

# Download dependencies and build the project
#RUN ./gradlew build

# Copy the compiled JAR file
COPY build/libs/free_market_api.jar /app/

# Expose port 9090 to the outside world
EXPOSE 9999

# Specify the command to run on container start
CMD ["java", "-jar", "free_market_api.jar"]