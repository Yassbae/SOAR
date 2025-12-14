# --- Build WAR (JDK 17) ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
# Skip totalement les tests (même compilation)
RUN mvn -q -Dmaven.test.skip=true package

# --- Run Payara Server Full 6 avec JDK 17 ---
FROM payara/server-full:6.2024.8-jdk17
ENV PAYARA_HOME=/opt/payara
# Déposer le WAR et préparer un auto-deploy explicite
COPY --from=build /app/target/bankease.war ${PAYARA_HOME}/glassfish/domains/domain1/autodeploy/bankease.war
# Forcer le déploiement au boot (idempotent)
RUN printf "deploy --name bankease --contextroot bankease ${PAYARA_HOME}/glassfish/domains/domain1/autodeploy/bankease.war\n" \
    > ${PAYARA_HOME}/config/post-boot-commands.asadmin
EXPOSE 8080 4848
