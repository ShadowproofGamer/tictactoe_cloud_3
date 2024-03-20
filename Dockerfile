FROM openjdk:21-jdk
EXPOSE 8080
COPY build/libs/tictactoe_cloud_3-1.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]