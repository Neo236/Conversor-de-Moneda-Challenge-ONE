# syntax=docker/dockerfile:1

# --- Etapa de build: compila y arma la distribución con su script de arranque ---
FROM gradle:8.10-jdk21 AS build
WORKDIR /src

# Primero solo los archivos de build: si no cambian, esta capa (y la descarga de
# dependencias) se reutiliza entre builds.
COPY settings.gradle build.gradle ./
RUN gradle dependencies --no-daemon --quiet || true

COPY src ./src
RUN gradle installDist --no-daemon --quiet

# --- Etapa de runtime: solo el JRE y la app, sin Gradle ni el JDK ---
FROM eclipse-temurin:21-jre AS runtime

# Usuario sin privilegios: la app no necesita root para nada.
RUN groupadd -r conversor && useradd -r -g conversor -m -d /home/conversor conversor

COPY --from=build /src/build/install/conversor /opt/conversor

# WORKDIR escribible: acá se crean historial.json y logs/ en tiempo de ejecución.
WORKDIR /data
RUN chown -R conversor:conversor /data
USER conversor

# La API Key se pasa por entorno (-e EXCHANGE_RATE_API_KEY=...). Si falta, la app la
# pide por teclado (queda solo en memoria) o sigue sin clave con las tasas del día.
ENTRYPOINT ["/opt/conversor/bin/conversor"]
