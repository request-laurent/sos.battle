FROM maven:3.8.1-openjdk-11 as mvnbuilder
LABEL maintainer="sos.ninja@ooutlook.com"

COPY . /build
WORKDIR /build
RUN /build/build.sh


FROM tomcat
LABEL maintainer="sos.ninja@ooutlook.com"

COPY --from=mvnbuilder /build/target/battle.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
