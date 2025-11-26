# Build stage - Amazon Corretto 24 com Maven instalado manualmente
FROM amazoncorretto:24-al2023 AS build

# Instalar Maven
RUN yum install -y maven && yum clean all

WORKDIR /app

# Copiar arquivos de configuração do Maven primeiro (para cache de layers)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Copiar frontend para resources
COPY frontend ./src/main/resources/frontend

# Build da aplicação (pular testes para build mais rápido)
RUN mvn clean package -DskipTests -Dspring.profiles.active=prod

# Runtime stage - Amazon Corretto 24 (mais leve, Alpine)
FROM amazoncorretto:24-alpine

WORKDIR /app

# Instalar wget para healthcheck e criar usuário não-root
RUN apk add --no-cache wget curl && \
    addgroup -S spring && adduser -S spring -G spring

# Copiar JAR do build stage
COPY --from=build /app/target/*.jar app.jar

# Mudar ownership do jar
RUN chown spring:spring app.jar

# Usar usuário não-root
USER spring:spring

# Expor porta (usar porta do range do Fasitech: 5030-5039)
EXPOSE 5030

# Variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=fasitech
ENV SERVER_PORT=5030
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
ENV TZ=America/Sao_Paulo

# Configurações de ordem automática
ENV ORDEM_AUTOMATICA_ENABLED=true
ENV ORDEM_AUTOMATICA_DIAS_ENTREGA=7

# Health check melhorado
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:5030/api/health || exit 1

# Executar aplicação com configurações otimizadas
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
