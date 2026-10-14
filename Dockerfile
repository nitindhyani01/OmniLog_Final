# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /src

# Cache dependencies separately from source
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- runtime stage ----------
FROM tomcat:11-jdk21

# Clean out the default welcome app
RUN rm -rf /usr/local/tomcat/webapps/*

# Drop our WAR in
COPY --from=build /src/target/OmniLog.war /usr/local/tomcat/webapps/OmniLog.war

EXPOSE 8080

CMD ["catalina.sh", "run"]