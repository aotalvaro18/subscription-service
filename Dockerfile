# Multi-stage build para producir el JAR en la etapa 'build'
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -f pom.xml clean package -DskipTests

# Runtime stage: Solo contiene el JRE y el JAR
FROM amazoncorretto:21-alpine
WORKDIR /app

# Instalar fuentes necesarias (si subscription-service necesita generar PDFs/documentos)
RUN apk add --no-cache fontconfig ttf-dejavu

# Copia el JAR compilado de la etapa 'build'
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]