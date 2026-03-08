# Multi-stage Dockerfile for Spring Boot Application (Java 25)

# Stage 1: Build the application
FROM gradle:8.11-jdk25-alpine AS build

WORKDIR /app

# Copy gradle wrapper and build files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer) - this improves build caching
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application (skip tests for Docker build, run separately in CI/CD)
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Install wget, create directories, setup user, and set permissions in one layer
RUN apk add --no-cache wget && \
    mkdir -p /app/uploads && \
    addgroup -S spring && adduser -S spring -G spring

# Copy the jar from build stage with explicit name
COPY --from=build /app/build/libs/task-manager-backend-*.jar app.jar

# Set proper ownership
RUN chown -R spring:spring /app

USER spring:spring

# Expose the application port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/task-manager/actuator/health || exit 1

# JVM optimization for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+OptimizeStringConcat \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application with optimized JVM settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

