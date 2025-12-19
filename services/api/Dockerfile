FROM gradle:7.6-jdk17 AS builder
WORKDIR /workspace
COPY . /workspace
RUN gradle bootJar --no-daemon || true

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
