FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build config first to maximize layer caching.
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Copy source and build a fresh boot JAR from current code.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]