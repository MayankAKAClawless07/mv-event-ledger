FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY event-ledger-domain/pom.xml event-ledger-domain/pom.xml
COPY event-gateway-api/pom.xml event-gateway-api/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY event-ledger-domain/src event-ledger-domain/src
COPY event-gateway-api/src event-gateway-api/src
RUN mvn -pl event-gateway-api -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/event-gateway-api/target/event-gateway-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
