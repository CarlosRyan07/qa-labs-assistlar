FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S assistlar && adduser -S assistlar -G assistlar
COPY --from=build /workspace/target/assistlar-*.jar app.jar
USER assistlar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
