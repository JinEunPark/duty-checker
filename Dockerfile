# ---- Build Stage ----
FROM gradle:8.13-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
