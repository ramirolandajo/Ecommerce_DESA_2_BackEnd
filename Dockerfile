# Etapa 1: Builder
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiamos solo el pom para caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
FROM amazoncorretto:21
WORKDIR /app

# Copiamos el JAR generado
COPY --from=builder /app/target/Ecommerce-0.0.1-SNAPSHOT.jar app.jar

# Exponemos puerto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
