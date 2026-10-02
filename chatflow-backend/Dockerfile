FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --create-home --uid 10001 chatflow
COPY --from=build /workspace/target/chatflow-backend-*.jar /app/chatflow-backend.jar
RUN chown -R chatflow:chatflow /app
USER chatflow
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/chatflow-backend.jar"]
