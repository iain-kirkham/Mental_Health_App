FROM eclipse-temurin:21-jre

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file built by Gradle
COPY build/libs/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]