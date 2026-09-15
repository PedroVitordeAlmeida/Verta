# ===== Etapa 1: build =====
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# Copia primeiro os arquivos de build para cache de dependências
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src

# Gera o fat jar (task fornecida pelo plugin io.ktor.plugin)
RUN gradle buildFatJar --no-daemon

# ===== Etapa 2: runtime =====
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Cria usuário não-root
RUN groupadd -r verta && useradd -r -g verta verta

COPY --from=build /app/build/libs/*-all.jar /app/verta-backend.jar

# Pasta para uploads de arquivos (mapear como volume no docker-compose)
RUN mkdir -p /app/uploads && chown -R verta:verta /app

USER verta

EXPOSE 3005

ENV PORT=3005

ENTRYPOINT ["java", "-jar", "/app/verta-backend.jar"]
