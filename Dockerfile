FROM maven:3.6.3-jdk-11-slim as build-hapi
WORKDIR /tmp/hapi-fhir-jpaserver-starter

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src/ /tmp/hapi-fhir-jpaserver-starter/src/
RUN mvn clean install -DskipTests

FROM tomcat:9.0.37-jdk11-openjdk-slim-buster

RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles

ADD /server.xml /usr/local/tomcat/conf/server.xml
ADD /cert/ /usr/local/tomcat/cert/
ADD /server.xml /
ADD /cert/ /


COPY --from=build-hapi /tmp/hapi-fhir-jpaserver-starter/target/*.war /usr/local/tomcat/webapps/

EXPOSE 8443
CMD ["catalina.sh", "run"]
