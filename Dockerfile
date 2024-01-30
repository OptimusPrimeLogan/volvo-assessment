#Stage 1
# initialize build and set base image for first stage
FROM maven:3.9.6-eclipse-temurin-17-alpine as build
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
# set working directory
WORKDIR /opt/demo
# copy just pom.xml
COPY pom.xml .
# go-offline using the pom.xml
RUN mvn dependency:go-offline
# copy your other files
COPY ./src ./src
# compile the source code and package it in a jar file
RUN mvn clean install -Dmaven.test.skip=true

#Stage 2
# set base image for second stage
FROM eclipse-temurin:17.0.10_7-jre-alpine
# set deployment directory
WORKDIR /opt/demo
# copy over the built artifact from the maven image
COPY --from=build /opt/demo/target/*.jar /opt/demo
# run the jar
ENTRYPOINT ["java", "-jar", "/opt/demo/calculator-0.0.1-SNAPSHOT.jar"]