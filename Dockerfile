FROM openjdk:11
ADD target/etl-service.war app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]