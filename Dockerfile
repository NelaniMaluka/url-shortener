# ==========================
# ===== Build Stage =======
# ==========================
# Use Temurin JDK 21 as the base
FROM eclipse-temurin:21 AS build

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies offline
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application (skip tests)
RUN mvn clean package -DskipTests

# ==========================
# ===== Runtime Stage =====
# ==========================
# Use lightweight JRE 21 for runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/url-shortner-0.0.1-SNAPSHOT.jar /app/app.jar

# Create a non-root user
RUN useradd -ms /bin/bash appuser
USER appuser

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Expose application port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
