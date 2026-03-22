FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY services/sport-analytics/sport-analytics-service/build/libs/*-all.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=20s \
    CMD wget -q --spider http://localhost:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
