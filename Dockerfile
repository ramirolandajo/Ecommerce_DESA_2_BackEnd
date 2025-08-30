# Etapa 1: Construcción
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
FROM amazoncorretto:21-jdk
WORKDIR /app
COPY --from=builder /app/target/Ecommerce-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
