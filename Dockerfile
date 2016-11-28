FROM registry.opensource.zalan.do/stups/openjdk:8-45

MAINTAINER team-glitch@zalando.de
COPY target/scala-2.11/metric-test.jar /opt/docker/metric-test.jar
WORKDIR /opt/docker
ADD ["target/scm-source.json", "/scm-source.json"]
RUN ["chown", "-R", "root:root", "."]
EXPOSE 8080 8778
USER root
CMD java $(appdynamics-agent) -jar metric-test.jar
