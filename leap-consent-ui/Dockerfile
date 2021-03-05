FROM openjdk:11-jdk

ARG JAR_FILE=target/leap-consent-ui-1.0-SNAPSHOT.war
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]
