# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Railway/Render use PORT env var
ENV PORT=8080
EXPOSE 8080

# Run with PORT for cloud platforms
CMD ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]
