# =====================================================================
# Etapa 1: build con Maven
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY src src

RUN mvn -B -DskipTests clean package

# =====================================================================
# Etapa 2: runtime con JRE liviano
# =====================================================================
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]