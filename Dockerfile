# --- Build Stage ---
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/ gradle/

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew bootJar --no-daemon

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ARG UID=10001
RUN useradd --uid ${UID} --create-home --shell /bin/bash appuser

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]