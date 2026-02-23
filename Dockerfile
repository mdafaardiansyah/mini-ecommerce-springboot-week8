# Multi-stage Dockerfile for Spring Boot Application
# This Dockerfile creates an optimized production-ready image

# ============================================================
# Stage 1: Build Stage
# Uses Maven to build the application
# ============================================================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (takes advantage of Docker layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
# -DskipTests: Tests are run in Jenkins, not during Docker build
# -B: Batch mode (non-interactive)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime Stage
# Uses lightweight JRE image to run the application
# ============================================================
FROM eclipse-temurin:17-jre-alpine

# Install dumb-init for proper signal handling and zombie process reaping
# https://github.com/Yelp/dumb-init
RUN apk add --no-cache dumb-init

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy jar file from builder stage
COPY --from=builder /app/target/Week8_Practice1-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Expose port (default: 8080, but Heroku will inject $PORT)
EXPOSE 8080

# Set JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER spring

# Entry point
# dumb-init enables proper signal handling (SIGTERM, SIGINT)
ENTRYPOINT ["dumb-init", "--"]

# Run the application
# Use $JAVA_OPTS for customizable JVM settings
# Use $PORT for configurable port (Heroku compatibility)
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /app/app.jar"]
