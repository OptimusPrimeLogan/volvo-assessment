FROM maven:3-openjdk
ADD . /code
WORKDIR /code
RUN mvn clean package -DskipTests=true -T 16
CMD mvn spring-boot:run