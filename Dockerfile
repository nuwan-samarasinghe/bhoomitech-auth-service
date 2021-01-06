# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=./target/auth-service-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} auth-service-0.0.1-SNAPSHOT.jar

# make entry point
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/auth-service-0.0.1-SNAPSHOT.jar"]

# Run the jar file
CMD ["java -Djava.security.egd=file:/dev/./urandom -jar /auth-service-0.0.1-SNAPSHOT.jar"]