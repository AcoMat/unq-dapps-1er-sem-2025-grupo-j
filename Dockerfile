# Use OpenJDK 21 as base image
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Install Chrome for Selenium WebDriver
RUN apt-get update && apt-get install -y wget gnupg2 curl unzip gnupg ca-certificates fonts-liberation \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-linux-signing-keyring.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux-signing-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
        > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Define build arguments for sensitive data
ARG FOOTBALL_DATA_API_KEY
ARG GOOGLE_API_KEY
ARG JWT_SECRET_KEY
ARG DB_PASSWORD

# Set environment variables from build arguments
# This prevents secrets from being stored in image history
ENV FOOTBALL_DATA_API_KEY=${FOOTBALL_DATA_API_KEY} \
    GOOGLE_API_KEY=${GOOGLE_API_KEY} \
    JWT_SECRET_KEY_SOCCER_GENIUS=${JWT_SECRET_KEY} \
    SOCCER_GENIUS_DB_PASSWORD=${DB_PASSWORD}

# Copy Gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Grant execution permission to Gradle wrapper
RUN chmod +x ./gradlew

# Build the application and run tests
CMD ["./gradlew", "build"]
