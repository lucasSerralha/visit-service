FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/visit-service-0.0.1-SNAPSHOT.jar app.jar
COPY opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ENV OTEL_SERVICE_NAME=visit-service
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4318
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar"]
