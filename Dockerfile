
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./
RUN chmod +x ./gradlew
COPY src ./src
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
RUN apt-get update && apt-get install -y wget gnupg \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' \
    && apt-get update \
    && apt-get install -y google-chrome-stable fonts-liberation \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/build/libs/*.jar application.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
