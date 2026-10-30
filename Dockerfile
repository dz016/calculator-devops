# ---- Stage 1: build the JAR with Maven + full JDK ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the POM first so the dependency layer is cached until pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Stage 2: slim runtime with just a JRE and the JAR ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as an unprivileged user instead of root.
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/calculator-*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
