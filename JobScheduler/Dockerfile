FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY TransactionalCache/pom.xml TransactionalCache/pom.xml
COPY JobScheduler/pom.xml JobScheduler/pom.xml
COPY JobScheduler/src JobScheduler/src
RUN mvn -q -pl JobScheduler -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/JobScheduler/target/JobScheduler-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]