# --- Etapa 1: Construcción ---
# Usamos una imagen con el JDK completo para compilar la aplicación con Gradle
FROM eclipse-temurin:21-jdk-jammy as builder

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /workspace

# Copiamos primero los archivos del wrapper de Gradle y los scripts de build
# Esto aprovecha el cache de Docker: si estos archivos no cambian,
# no se volverán a descargar las dependencias en cada build.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

# Damos permisos de ejecución al wrapper (necesario en algunos entornos)
RUN chmod +x ./gradlew

# Descargamos las dependencias (opcional, pero acelera builds posteriores)
# Descomenta si quieres usar el cache de dependencias de Docker
# RUN ./gradlew dependencies

# Copiamos el resto del código fuente del proyecto
COPY src ./src

# Compilamos la aplicación y empaquetamos en un JAR ejecutable (bootJar), saltando los tests
# El JAR se generará en /workspace/build/libs/
RUN ./gradlew bootJar -x test


# --- Etapa 2: Ejecución ---
# Usamos una imagen más ligera solo con el JRE, ya que no necesitamos compilar más
FROM eclipse-temurin:21-jre-jammy

# Establecemos el directorio de trabajo final
WORKDIR /app

# Creamos un usuario y grupo no-root para ejecutar la aplicación (buenas prácticas de seguridad)
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# --- Instalación de Google Chrome y dependencias ---
# Necesario para ejecutar Selenium/WebDriver en modo headless
# Se instala como root ANTES de cambiar al usuario 'appuser'
RUN apt-get update && apt-get install -y wget gnupg \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' \
    && apt-get update \
    && apt-get install -y google-chrome-stable fonts-liberation \
    && rm -rf /var/lib/apt/lists/*

# Copiamos el JAR compilado desde la etapa de construcción 'builder'
# Gradle normalmente pone los JARs en build/libs/
# Asumimos que tu JAR se llama algo como 'soccergenius-0.0.1-SNAPSHOT.jar'.
# Usamos un comodín para que funcione con diferentes nombres/versiones.
# Lo renombramos a 'application.jar' para simplificar el comando ENTRYPOINT.
COPY --from=builder /workspace/build/libs/*.jar application.jar

# Cambiamos la propiedad del directorio de la aplicación al usuario no-root
RUN chown -R appuser:appgroup /app

# Cambiamos al usuario no-root
USER appuser

# Exponemos el puerto por defecto en el que corre Spring Boot (ajusta si usas otro)
EXPOSE 8080

# El comando que se ejecutará cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
