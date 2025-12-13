# ==========================
# ===== Build Stage =======
# ==========================
# Use Maven + Temurin JDK 21 to compile the app
FROM maven:3.9.2-eclipse-temurin-21 AS build

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Download dependencies offline to speed up builds
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application into a JAR (skip tests since we'll run them separately in CI)
RUN mvn clean package -DskipTests

# ==========================
# ===== Runtime Stage =====
# ==========================
# Use lightweight Temurin JRE 17 for runtime (smaller image than full JDK)
FROM eclipse-temurin:17-jre

# Set working directory for the runtime container
WORKDIR /app

# --------------------------
# Copy the JAR from build stage
# --------------------------
# Important: Copy before switching to non-root user
COPY --from=build /app/target/url-shortner-0.0.1-SNAPSHOT.jar /app/app.jar

# --------------------------
# Create a non-root user for security
# --------------------------
RUN useradd -ms /bin/bash appuser
USER appuser

# --------------------------
# Set environment variables
# --------------------------
# Use production Spring profile
ENV SPRING_PROFILES_ACTIVE=prod

# JVM options (min/max heap)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# --------------------------
# Expose the port your Spring Boot app runs on
# --------------------------
EXPOSE 8080

# --------------------------
# Entry point to run the JAR
# --------------------------
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]