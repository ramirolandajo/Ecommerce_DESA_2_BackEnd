FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Instalamos Maven para construir el jar
RUN apt-get update && apt-get install -y maven

# Construimos el jar
RUN mvn clean package -DskipTests

# Copiamos el jar generado a app.jar
RUN cp target/Ecommerce-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
