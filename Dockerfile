# ---------- Etapa 1: build do JAR ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependencias primeiro (cache: so refaz se o pom.xml mudar)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compila o projeto (testes pulados: o contextLoads precisa de banco)
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Etapa 2: imagem final, so com o JRE ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Roda sem root
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

# Ajustado para a instancia Free do Render (512 MB RAM / 0.1 CPU)
# user.timezone: o servidor do Render roda em UTC; sem isso os horarios dos posts ficam 3h adiantados
ENV JAVA_OPTS="-Xmx300m -XX:MaxMetaspaceSize=128m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Duser.timezone=America/Sao_Paulo"

EXPOSE 8080
# O Render injeta a variavel PORT; o application.properties le ela
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
