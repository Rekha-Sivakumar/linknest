# Multi-stage Dockerfile for LinkNest

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal JRE runtime environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for persistent JSON storage
RUN mkdir -p /app/data

# Copy built JAR from build stage
COPY --from=build /app/target/linknest-1.0.0.jar app.jar

# Render assigns dynamic port via $PORT
ENV PORT=8080
ENV DATA_FILE=/app/data/linknest-data.json

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dlinknest.data-file=${DATA_FILE:-/app/data/linknest-data.json} -jar app.jar"]
