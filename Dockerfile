# Etapa 1: Construcción
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app

# Primero copiamos solo pom.xml y resolvemos dependencias (caché)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el resto del proyecto
COPY src ./src

# Construimos el JAR sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
FROM amazoncorretto:21
WORKDIR /app

# Copiamos el JAR generado desde builder
COPY --from=builder /app/target/Ecommerce-0.0.1-SNAPSHOT.jar app.jar

# Exponemos puerto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
