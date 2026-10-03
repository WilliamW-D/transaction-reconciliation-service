# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Production Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/transaction-reconciliation-service-1.0.0-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=postgres"]
