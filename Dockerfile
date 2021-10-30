FROM eclipse-temurin:11.0.12_7-jre-focal

EXPOSE 8080

COPY build/libs/persist-facts-0.0.1-SNAPSHOT.jar /opt/app.jar

ENTRYPOINT ["java" , "-jar", "/opt/app.jar"]