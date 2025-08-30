# Etapa 1: Construcción del JAR
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiamos todo el proyecto
COPY pom.xml .
COPY src ./src

# Construimos el JAR sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con solo el JAR
FROM amazoncorretto:21
WORKDIR /app

# Copiamos el JAR generado desde builder
COPY --from=builder /app/target/Ecommerce-0.0.1-SNAPSHOT.jar app.jar

# Exponemos puerto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
