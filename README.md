# Congestion Tax Calculator

Welcome the Volvo Cars Congestion Tax Calculator assignment.

This repository contains a developer [assignment](ASSIGNMENT.md) used as a basis for candidate intervew and evaluation.

Clone this repository to get started. Due to a number of reasons, not least privacy, you will be asked to zip your solution and mail it in, instead of submitting a pull-request. In order to maintain an unbiased reviewing process, please ensure to **keep your name or other Personal Identifiable Information (PII) from the code**.

# _The below comments are for the interview panel:_

The project can be run as ``mvn clean spring-boot:run``

The project is enriched with the following features:
- Open API generator plugin is being used, which helps in generating the API backbone and transfer objects that can be used in backend development. 
- Swagger is enabled along with OpenAPI spec, once the server is up, go to http://localhost:8080/swagger-ui/index.html
- Using micrometer data collection for observability metrics, the prometheus compatible metrics can be found http://localhost:8080/actuator/prometheus
- The metrics are emitted and collected from the controller class as an example, once the server is up, trigger some requests and go to http://localhost:8080/actuator/prometheus and the search for ProcessTollRequest.
- JaCoCo code coverage is available ``/target/site/jacoco/index.html``
- OWASP dependency check happens during verify goals and be found at ```/target/dependency-check-report.html```
- I have stacked the microserver, prometheus and grafana in a docker-compose file, simple type ``docker-compose build`` and ``docker-compose up -d``
- Check for metrics start with ProcessTollRequest

Important Links in DEV environment:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Prometheus: http://localhost:9090/graph
- Grafana: http://localhost:3000/ (default credentials -> admin/admin)

