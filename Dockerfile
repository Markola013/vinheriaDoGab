# 1. Fase de Build: Usamos a imagem Maven que já tem Java e o Maven instalado
# Isso garante que todas as libs de build necessárias estejam presentes.
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

# Definir o diretório de trabalho
WORKDIR /app

# Copiar os arquivos de build primeiro para aproveitar o cache do Docker (melhor performance)
# Isso só invalida o cache se o pom.xml mudar
COPY pom.xml .

# Copiar o Maven Wrapper (CRÍTICO para resolver seu erro)
# Garante que .mvn/wrapper/ seja copiado antes da execução do mvnw
COPY .mvn /app/.mvn
COPY mvnw .

# Torna o script executável
RUN chmod +x mvnw

# Copiar o restante do código
COPY src /app/src

# 2. Build da aplicação: O comando 'mvnw' agora deve funcionar
# Use o ./mvnw
RUN ./mvnw -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install

# ----------------------------------------------------------------------
# 2. Fase de Runtime: Usamos uma imagem JRE menor e mais leve para produção
# ----------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia a aplicação construída da fase de 'build'
COPY --from=build /app/target/quarkus-app /app/target/quarkus-app

# Executa o aplicativo Quarkus
CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]