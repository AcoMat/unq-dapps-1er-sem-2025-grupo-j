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

# Set environment variables
ENV FOOTBALL_DATA_API_KEY=857fc286146d4229969554d43038d22e \
    GOOGLE_API_KEY=AIzaSyCbGx3kqdDlTzn9dV-xjNfpD_GU3nUCPmc \
    JWT_SECRET_KEY_SOCCER_GENIUS=12345 \
    SOCCER_GENIUS_DB_PASSWORD=password

# Copy Gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Grant execution permission to Gradle wrapper
RUN chmod +x ./gradlew

# Build the application and run tests
CMD ["./gradlew", "build"]
