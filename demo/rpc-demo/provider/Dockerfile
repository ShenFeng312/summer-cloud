#FROM apache/skywalking-java-agent:8.9.0-java11
FROM openjdk:11.0.16-jdk


ADD ./target/*.jar /app/app.jar

# 暴露对应的端口
EXPOSE 8080
RUN groupadd -r app && useradd -r -g app app
USER app

ENTRYPOINT exec java -jar /app/app.jar