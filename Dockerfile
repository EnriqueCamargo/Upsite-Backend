# Etapa de construcción
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# 1. Copiar archivos de gradle para caché
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 2. Compilar el JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Etapa de ejecución
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# 3. Copiamos el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

# 4. Render asigna un puerto dinámico.
# Definimos el 8080 por defecto pero Render lo sobrescribirá.
ENV PORT=8080
EXPOSE 8080

# 5. Ejecución: Vital pasar el -Dserver.port=${PORT}
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]