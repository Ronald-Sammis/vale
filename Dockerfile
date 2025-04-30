# Etapa 1: construir la aplicación con Maven y Java 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# Etapa 2: imagen final solo con el JAR para ejecutar
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/vale-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
